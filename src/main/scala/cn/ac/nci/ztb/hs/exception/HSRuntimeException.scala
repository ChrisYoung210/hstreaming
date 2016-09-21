package cn.ac.nci.ztb.hs.exception

/**
  * Created by Young on 16-9-18.
  */
class HSRuntimeException(message: String, cause: Throwable)
  extends RuntimeException(message, cause) {
  def this(message: String) = this(message, null)
  def this(cause: Throwable) = this("", cause)

}
