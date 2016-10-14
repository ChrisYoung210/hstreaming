package cn.ac.nci.ztb.hs.resource.worker

/**
  * @author Young
  * @version 1.0
  *          CreateTime: 16-10-13 上午10:57
  */
private[worker] trait ResourceDetector[T] extends Runnable {

  var value: T = _
  var timestamp: Long = 0

  val lock = new Object

  var interrupt = false

  def setInterrupt() { interrupt = true }

  /**
    * 获取最新的资源探测结果，要求最新的结果探测的时间晚于要求的时间
    * @param lastTimestamp 最新的探测结果获取的时间需要晚于改时间
    * @return 若结果的时间戳满足条件则返回最新的值，否则返回null
    */
  def getLastDetectValue(lastTimestamp: Long): T = {
    lock.synchronized {
      if (lastTimestamp < timestamp) value
      else null.asInstanceOf[T]
    }
  }
}
