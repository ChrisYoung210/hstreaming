package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress

/**
  * Created by Young on 16-8-30.
  */
trait RpcEngine {

  /**
    * 获取RPC Client端的动态代理实例
    * @param clazz  协议类型（一般为interface或者trait）
    * @param address  RPC Server地址
    * @tparam T 协议类型
    * @return 根据协议类型生成的RPC动态代理实例
    */
  def getProxy[T](clazz : Class[T], address : InetSocketAddress) : T

  /**
    * 获取RPC Server端的服务实例
    * @param address  绑定的本地地址
    * @return RPC Server
    */
  def getServer(address : InetSocketAddress) : Server
}
