package cn.ac.nci.ztb.hs.common

/**
  * Created by Young on 16-8-30.
  */
trait Service {
  def init : Service

  def start : Service

  def stop : Service

  object State extends Enumeration {
    type State = Value
    val UNINITED, INITED, STARTED, STOPED = Value
  }

}
