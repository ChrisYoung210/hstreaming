package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress

/**
  * Created by Young on 16-8-30.
  */
trait RpcEngine {
  def getProxy[T](clazz : Class[T], address : InetSocketAddress) : T

  def getServer(address : InetSocketAddress) : Server
}
