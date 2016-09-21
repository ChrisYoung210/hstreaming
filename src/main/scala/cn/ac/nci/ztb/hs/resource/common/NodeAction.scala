package cn.ac.nci.ztb.hs.resource.common

/**
  * Created by Young on 16-9-21.
  */
object NodeAction extends Enumeration {
  type NodeAction = Value
  val NORMAL, RESYNC, SHUTDOWN = Value
}
