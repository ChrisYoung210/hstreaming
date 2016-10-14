package cn.ac.nci.ztb.hs.resource.common

import java.util.concurrent.atomic.AtomicInteger

/**
  * Created by Young on 16-9-18.
  */
case class WorkerId private(id : Int) extends Serializable {

  def getId = id

  override def hashCode = id.hashCode

  override def toString = id + ""
}

object WorkerId {
  private lazy val nextId = new AtomicInteger

  def getNewWorkerId = WorkerId(nextId.getAndIncrement)
}
