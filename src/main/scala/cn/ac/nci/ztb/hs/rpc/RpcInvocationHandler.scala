package cn.ac.nci.ztb.hs.rpc

import java.lang.reflect.InvocationHandler

import cn.ac.nci.ztb.hs.io.Writable

/**
  * Created by Young on 16-9-2.
  */
trait RpcInvocationHandler[T <: Writable] extends InvocationHandler {
  def putResponse(response : T)
}
