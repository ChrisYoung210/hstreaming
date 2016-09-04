package cn.ac.nci.ztb.hs.rpc
package kryo

import java.lang.reflect.{InvocationTargetException, Method}
import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.common.Configuration
import cn.ac.nci.ztb.hs.io.Writable
import com.esotericsoftware.kryo.Kryo
import io.netty.channel.{ChannelHandlerContext, ChannelInitializer, SimpleChannelInboundHandler}
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import org.apache.commons.pool2.impl.{DefaultPooledObject, GenericObjectPool, GenericObjectPoolConfig}
import org.apache.commons.pool2.{BasePooledObjectFactory, PooledObject}
import org.objenesis.strategy.SerializingInstantiatorStrategy
import org.slf4j.LoggerFactory

import scala.collection.mutable.HashMap
import scala.language.postfixOps
import scala.reflect.ClassTag

/**
  * Created by Young on 16-9-1.
  */
class KryoRpcEngine extends RpcEngine {

  override def getProxy[T: ClassTag](address: InetSocketAddress): T = ???

  override def getServer(address: InetSocketAddress): Server = ???

  private class KryoRpcServer(address: InetSocketAddress) extends Server(address) {

    private lazy val protocolNameInsMap = new HashMap[Class[_], AnyRef]

    override def addProtocolAndInstance[T <: AnyRef](clazz: Class[T], instance: T): Boolean =
      if (protocolNameInsMap contains clazz) false
      else { protocolNameInsMap.+=((clazz, instance)); true}

    override def getInitializer: ChannelInitializer[SocketChannel] = {
      new ChannelInitializer[SocketChannel] {
        override def initChannel(ch: SocketChannel): Unit = {
          ch pipeline() addLast
            new KryoRequestDecoder(KryoRpcPool getServerKryoPool) addLast
            new KryoResponseEncoder(KryoRpcPool getServerKryoPool) addLast
            new SimpleChannelInboundHandler[KryoRequestWrapper] {
              override def channelRead0(ctx: ChannelHandlerContext,
                                        msg: KryoRequestWrapper) {
                try {
                  val response = msg.call(getInstance(msg getProtocolClass))
                  ctx writeAndFlush new KryoResponseWrapper(response,
                    msg getRequestId, null)
                } catch {
                  case e : InvocationTargetException => {
                    RPC.logger warn(e toString, e)
                    ctx writeAndFlush new KryoResponseWrapper(
                      null, msg getRequestId, e getCause)
                  }
                }

              }
            }

        }
      }
    }

    def getInstance(clazz : Class[_]) = protocolNameInsMap get clazz
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
            new KryoRpcClientHandler(getInvoker asInstanceOf)
        }
      }
    }
  }

  private class Invoker(address: InetSocketAddress,
                        protocol : Class[_]) extends
    RpcInvocationHandler[KryoResponseWrapper] {

    private val client = new KryoRpcClient(address, this)

    def getResponse(requestId : Long) : KryoResponseWrapper = null

    override def putResponse(response: KryoResponseWrapper): Unit = ???

    override def invoke(proxy: scala.Any,
                        method: Method,
                        args: Array[AnyRef]) = {
      val params = new Array[Writable](args.length)
      for (i <- args.indices) params(i) = args(i) asInstanceOf

      val requestId = client getNextRequestId
      val requestWrapper = new KryoRequestWrapper(protocol,
        method getName, params, requestId)
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
