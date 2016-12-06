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
import ImplicityInstance._
/**
  * Created by Young on 16-9-1.
  * 类KryoRpcEngine用于完成基于Kryo序列化的RPC传输，要求RPC协议中所有方法的所有参数以及返回值
  * 均实现了Serializable接口，或者实现Writable、ComparableWritable接口。
  */
class KryoRpcEngine extends RpcEngine {

  /**
    * 用于存储协议的动态代理实例
    */
  private lazy val PROXY_CACHE = new mutable.WeakHashMap[
    InetSocketAddress, mutable.WeakHashMap[Class[_], AnyRef]]

  /**
    * 获取RPC Client端的动态代理实例
    * @param clazz  协议类型（一般为interface或者trait）
    * @param address  RPC Server地址
    * @tparam T 协议类型
    * @return 根据协议类型生成的RPC动态代理实例
    */
  override def getProxy[T](clazz : Class[T],
                           address: InetSocketAddress) : T = {
    PROXY_CACHE synchronized {
      val tempMap = PROXY_CACHE getOrElseUpdate(address,
        new mutable.WeakHashMap[Class[_], AnyRef])
      tempMap.getOrElseUpdate(clazz, Proxy newProxyInstance(clazz getClassLoader,
        Array[java.lang.Class[_]](clazz), new Invoker(address, clazz))).asInstanceOf[T]
    }
  }

  /**
    * 获取RPC Server端的服务实例
    * @param address  绑定的本地地址
    * @return RPC Server
    */
  override def getServer(address: InetSocketAddress): Server = new KryoRpcServer(address)

  /**
    * Kryo RPC引擎的Server端，用于接收、处理Client端的RPC请求
    * @param address  RPC Server绑定的本地地址
    */
  private class KryoRpcServer(address: InetSocketAddress) extends Server(address) {

    private lazy val protocolNameInsMap = new mutable.HashMap[Class[_], (AnyRef, Array[Method])]

    override def addProtocolAndInstance[T <: AnyRef](clazz: Class[T], instance: T): Boolean =
      if (protocolNameInsMap contains clazz) false
      else {
        protocolNameInsMap += clazz -> (instance, clazz.getMethods.sorted.map{
          x => {
            KryoRpcEngine.logger debug s"${x.toString}"
            x.setAccessible(true)
            x
          }
        })
        true
      }

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
                  val instanceAndMethod = getInstanceAndMethod(msg getProtocolClass, msg getMethodId)
                  //KryoRpcEngine.logger debug s"${instanceAndMethod._2.toString}"
                  val response = instanceAndMethod._2.invoke(instanceAndMethod._1, msg getRequestParameters: _*)
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

    def getInstanceAndMethod(clazz : Class[_], methodId: Int) =
      (protocolNameInsMap(clazz)._1, protocolNameInsMap(clazz)._2.apply(methodId))

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

    private val methods = {
      val tmp = new mutable.HashMap[Method, Int]()
      val ms = protocol.getMethods.sorted
      for (i <- ms.indices) {
        tmp += ms(i) -> i
        KryoRpcEngine.logger debug s"Inovker ${ms(i)}"
      }
      tmp
    }

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
        methods(method), args, requestId)
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
    config setMaxTotal(Configuration getIntOrDefault("kryo.server.number", 20))
    config
  })

  def getServerKryoPool = serverPool

  lazy val clientPool = new GenericObjectPool[Kryo](new KryoPool, {
    val config = new GenericObjectPoolConfig
    config setMaxTotal(Configuration getIntOrDefault("kryo.client.number", 5))
    config
  })

  def getClientKryoPool = clientPool

}
