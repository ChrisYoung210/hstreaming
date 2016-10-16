package cn.ac.nci.ztb.hs.rpc
package kryo

import java.lang.reflect.{InvocationTargetException, Method, Proxy}
import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.common.Configuration
import cn.ac.nci.ztb.hs.utils.BlockingHashMap
import com.esotericsoftware.kryo.Kryo
import io.netty.channel.socket.SocketChannel
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import org.apache.commons.pool2.impl.{DefaultPooledObject, GenericObjectPool, GenericObjectPoolConfig}
import org.apache.commons.pool2.{BasePooledObjectFactory, PooledObject}
import org.objenesis.strategy.SerializingInstantiatorStrategy
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by Young on 16-9-1.
  * 类KryoRpcEngine用于完成基于Kryo序列化的RPC传输，要求RPC协议中所有方法的所有参数以及返回值
  * 均实现了Serializable接口，或者实现Writable、ComparableWritable接口。
  */
class KryoRpcEngine extends RpcEngine {

  private lazy val PROXY_CACHE = new mutable.WeakHashMap[
    InetSocketAddress, mutable.WeakHashMap[Class[_], AnyRef]]

  override def getProxy[T](clazz : Class[T],
                           address: InetSocketAddress) : T = {
    PROXY_CACHE synchronized {
      val tempMap = PROXY_CACHE getOrElseUpdate(address,
        new mutable.WeakHashMap[Class[_], AnyRef])
      tempMap.getOrElseUpdate(clazz, Proxy newProxyInstance(clazz getClassLoader,
        Array[java.lang.Class[_]](clazz), new Invoker(address, clazz))).asInstanceOf[T]
    }
  }

  override def getServer(address: InetSocketAddress): Server = new KryoRpcServer(address)

  private class KryoRpcServer(address: InetSocketAddress) extends Server(address) {

    private lazy val protocolNameInsMap = new mutable.HashMap[Class[_], AnyRef]

    override def addProtocolAndInstance[T <: AnyRef](clazz: Class[T], instance: T): Boolean =
      if (protocolNameInsMap contains clazz) false
      else { protocolNameInsMap += clazz -> instance; true}

    override def getInitializer: ChannelInitializer[SocketChannel] = {
      new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          ch pipeline() addLast
            new KryoRequestDecoder(KryoRpcPool getServerKryoPool) addLast
            new KryoResponseEncoder(KryoRpcPool getServerKryoPool) addLast
            new SimpleChannelInboundHandler[KryoRequestWrapper] {

              override def channelActive(ctx: ChannelHandlerContext): Unit = {
                KryoRpcEngine.logger debug "build connection with " + ctx.channel().remoteAddress()
                super.channelActive(ctx)
              }

              override def channelRead0(ctx: ChannelHandlerContext,
                                        msg: KryoRequestWrapper) {
                try {
                  val response = msg.call(getInstance(msg getProtocolClass))
                  ctx writeAndFlush new KryoResponseWrapper(response,
                    msg getRequestId, null)
                } catch {
                  case e : InvocationTargetException =>
                    KryoRpcEngine.logger warn(e toString, e)
                    ctx writeAndFlush new KryoResponseWrapper(
                      null, msg getRequestId, e getCause)
                }
              }
            }
        }
      }
    }

    def getInstance(clazz : Class[_]) = protocolNameInsMap(clazz)
  }

  private class KryoRpcClient(address : InetSocketAddress,
                              invoker : RpcInvocationHandler[KryoResponseWrapper])
    extends Client(address, invoker) {

    override def getInitializer: ChannelInitializer[SocketChannel] = {
      new ChannelInitializer[SocketChannel] {

        override def initChannel(ch: SocketChannel) {
          ch pipeline() addLast
            new KryoRequestEncoder(KryoRpcPool.getClientKryoPool) addLast
            new KryoResponseDecoder(KryoRpcPool.getClientKryoPool) addLast
            new KryoRpcClientHandler(getInvoker
              .asInstanceOf[RpcInvocationHandler[KryoResponseWrapper]])
        }
      }
    }
  }

  private class Invoker(address: InetSocketAddress,
                        protocol : Class[_]) extends
    RpcInvocationHandler[KryoResponseWrapper] {

    private val client = new KryoRpcClient(address, this)
    client.init
    client.start

    private lazy val responses =
      new BlockingHashMap[Long, KryoResponseWrapper](Configuration
        getIntOrDefault("rpc.timeout", 10000))

    def getResponse(requestId : Long) = {
      KryoRpcEngine.logger debug "Try to get Response by the id: " + requestId
      responses get requestId
    }

    override def putResponse(response: KryoResponseWrapper): Unit = {
      KryoRpcEngine.logger debug "Put the response whose id is " + response.getRequestId
      responses put(response getRequestId, response)
    }

    override def invoke(proxy: scala.Any,
                        method: Method,
                        args: Array[AnyRef]) = {
      val requestId = client getNextRequestId
      val requestWrapper = new KryoRequestWrapper(protocol,
        method.getName, args, requestId)
      client send requestWrapper
      val responseWrapper = getResponse(requestId)
      if (responseWrapper hasException) {
        KryoRpcEngine.logger warn(responseWrapper.getException toString,
          responseWrapper getException)
        throw responseWrapper.getException
      }
      responseWrapper getResponse
    }
  }
}

object KryoRpcEngine {

  val logger = LoggerFactory getLogger classOf[KryoRpcEngine]

}

/**
  *
  */
private object KryoRpcPool {

  class KryoPool extends BasePooledObjectFactory[Kryo] {
    override def wrap(obj: Kryo): PooledObject[Kryo] =
      new DefaultPooledObject[Kryo](obj)

    override def create(): Kryo = {
      val kryo = new Kryo
      kryo setInstantiatorStrategy new SerializingInstantiatorStrategy
      kryo register classOf[KryoRequestWrapper]
      kryo register classOf[KryoResponseWrapper]
      kryo
    }
  }

  lazy val serverPool = new GenericObjectPool[Kryo](new KryoPool, {
    val config = new GenericObjectPoolConfig
    config setMaxTotal(Configuration getIntOrDefault("server.kryo.number", 20))
    config
  })

  def getServerKryoPool = serverPool

  lazy val clientPool = new GenericObjectPool[Kryo](new KryoPool, {
    val config = new GenericObjectPoolConfig
    config setMaxTotal(Configuration getIntOrDefault("client.kryo.number", 5))
    config
  })

  def getClientKryoPool = clientPool

}
