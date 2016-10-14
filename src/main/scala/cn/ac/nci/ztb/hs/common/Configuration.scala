package cn.ac.nci.ztb.hs.common

import scala.collection.mutable

/**
  * Created by Young on 16-8-30.
  */
object Configuration {

  private lazy val elements = new mutable.HashMap[String, String]

  def getIntOrDefault(key : String, default : Int) = {
    elements getOrElseUpdate(key, default.toString) toInt
  }

  def get(key: String) = {
    elements(key)
  }

  def getOrDefault(key: String, default: Any) = {
    elements getOrElseUpdate(key, default toString)
  }
}
