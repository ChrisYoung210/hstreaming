package cn.ac.nci.ztb.hs.common

/**
  * Created by Young on 16-8-30.
  */
trait Service {
  def init : Boolean

  def start : Boolean

  def stop : Boolean

  object State extends Enumeration {
    type State = Value
    val UNINITED, INITED, STARTED, STOPED = Value
  }

}
