package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.rpc.kryo.KryoRpcEngine
import cn.ac.nci.ztb.hs.utils.Reflections
import org.slf4j.LoggerFactory

import scala.collection.mutable._
import scala.reflect._

/**
  * Created by Young on 16-8-30.
  */
object RPC {
  val logger = LoggerFactory.getLogger(RPC.getClass)

  private val PROTOCOL_ENGINES = new WeakHashMap[Class[_ <: RpcEngine], RpcEngine]

  private val SERVER_CACHE = new HashMap[Class[_ <: RpcEngine], Map[InetSocketAddress, Server]]

  private def getProtocolEngine[T <: RpcEngine : ClassTag] = {
    PROTOCOL_ENGINES.synchronized {
      val clazz = classTag[T].runtimeClass.asInstanceOf[Class[_ <: RpcEngine]]
      PROTOCOL_ENGINES.getOrElseUpdate(clazz, Reflections newInstance clazz)
    }
  }

  def getServer(port : Int, host : String = "0.0.0.0") = {
    val address = new InetSocketAddress({if(host == null) "0.0.0.0" else host}, port)
    val maps = SERVER_CACHE getOrElseUpdate(classOf[KryoRpcEngine], new HashMap[InetSocketAddress, Server])
    maps getOrElseUpdate(address, getProtocolEngine[KryoRpcEngine] getServer address)
  }

  def getProxy[T](clazz : Class[T], address : InetSocketAddress) : T =
    getProtocolEngine[KryoRpcEngine] getProxy(clazz, address)
}
