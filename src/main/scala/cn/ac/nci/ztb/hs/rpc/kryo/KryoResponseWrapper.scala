package cn.ac.nci.ztb.hs.rpc.kryo

import cn.ac.nci.ztb.hs.io.Writable

/**
  * Created by Young on 16-9-2.
  */
class KryoResponseWrapper(response : Writable,
                          requestId : Long,
                          exception : java.lang.Throwable)
  extends Writable {

  def getResponse = response

  def getRequestId = requestId

  def hasException = exception == null

  def getException = exception

}
