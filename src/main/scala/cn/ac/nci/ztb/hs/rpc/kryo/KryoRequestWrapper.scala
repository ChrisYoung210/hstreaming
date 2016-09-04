package cn.ac.nci.ztb.hs.rpc.kryo

import cn.ac.nci.ztb.hs.io.Writable

/**
  * Created by Young on 16-9-2.
  */
class KryoRequestWrapper(protocolClazz : Class[_],
                         methodName : String,
                         requestParameters : Array[Writable],
                         requestId : Long) extends Writable {
  def call(instance : AnyRef) : Writable = {
    val parametersType =
      new Array[Class[_]](requestParameters length)
    for (i <- requestParameters.indices)
      parametersType(i) = requestParameters(i) getClass
    val method = instance getClass() getMethod(methodName, parametersType : _*)
    method setAccessible true
    method invoke(instance, requestParameters) asInstanceOf
  }

  def getProtocolClass = protocolClazz

  def getRequestId = requestId
}