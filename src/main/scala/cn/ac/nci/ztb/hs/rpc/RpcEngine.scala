package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress

import scala.reflect.ClassTag

/**
  * Created by Young on 16-8-30.
  */
trait RpcEngine {
  def getProxy[T : ClassTag](address : InetSocketAddress) : T

  def getServer(address : InetSocketAddress) : Server
}
