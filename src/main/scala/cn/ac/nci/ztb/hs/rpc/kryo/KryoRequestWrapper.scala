package cn.ac.nci.ztb.hs.rpc.kryo


import java.lang.reflect.Method

import scala.language.postfixOps

/**
  * Created by Young on 16-9-2.
  */
class KryoRequestWrapper(protocolClazz : Class[_],
                         methodName : String,
                         requestParameters : Array[AnyRef],
                         requestId : Long) extends Serializable {

  def getRequestParameters(index: Int) = requestParameters(index)

  def call(instance : AnyRef) : AnyRef = {
    val parametersType =
      new Array[Class[_]](requestParameters length)
    for (i <- requestParameters.indices) {
      if (requestParameters(i) == null) KryoRpcEngine.logger warn s"The ${i}th parameter is null."
      else parametersType(i) = requestParameters(i) getClass
    }
    val method = instance.getClass getMethod(methodName, parametersType : _*)
    method setAccessible true
    method.invoke(instance, requestParameters: _*)
  }

  def getProtocolClass = protocolClazz

  def getRequestId = requestId
}
/*
class KryoRequestWrapper(protocolClazz : Class[_],
                         method: Method,
                         requestParameters : Array[AnyRef],
                         requestId : Long) extends Serializable {

  def getRequestParameters(index: Int) = requestParameters(index)

  def call(instance : AnyRef) : AnyRef = {
    //val method = instance.getClass getMethod(methodName, parametersType : _*)
    method setAccessible true
    method.invoke(instance, requestParameters: _*)
  }

  def getProtocolClass = protocolClazz

  def getRequestId = requestId
}*/
