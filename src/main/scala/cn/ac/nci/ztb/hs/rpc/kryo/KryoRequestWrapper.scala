package cn.ac.nci.ztb.hs.rpc.kryo


import scala.language.postfixOps

/**
  * Created by Young on 16-9-2.
  */
class KryoRequestWrapper(protocolClazz : Class[_],
                         methodName : String,
                         requestParameters : Array[AnyRef],
                         requestId : Long) extends Serializable {

  {
    KryoRpcEngine.logger debug s"In client KryoRequestWrapper ${requestParameters(0)}"
  }

  def getRequestParameters(index: Int) = requestParameters(index)

  def call(instance : AnyRef) : AnyRef = {
    val parametersType =
      new Array[Class[_]](requestParameters length)
    for (i <- requestParameters.indices)
      parametersType(i) = requestParameters(i) getClass
    val method = instance.getClass getMethod(methodName, parametersType : _*)
    method setAccessible true
    method.invoke(instance, requestParameters: _*)
  }

  def getProtocolClass = protocolClazz

  def getRequestId = requestId
}
