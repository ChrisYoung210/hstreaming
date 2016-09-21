package cn.ac.nci.ztb.hs.resource.common

import java.util.concurrent.atomic.AtomicInteger

import cn.ac.nci.ztb.hs.io.Writable

/**
  * Created by Young on 16-9-18.
  */
class WorkerId private(id : Int) extends Writable {

  /*override def equals(obj : Any): Boolean = {
    if (obj == null) false
    else obj match {
      case id1: WorkerId => id == id1.id
      case _ => false
    }
  }*/

  def getId = id

  override def hashCode = id.hashCode

  override def toString = id + ""
}

object WorkerId {
  private lazy val nextId = new AtomicInteger

  def getNewWorkerId = new WorkerId(nextId.getAndIncrement)
}
