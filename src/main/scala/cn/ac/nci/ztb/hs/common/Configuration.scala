package cn.ac.nci.ztb.hs.common

import scala.collection.mutable._

/**
  * Created by Young on 16-8-30.
  */
object Configuration {

  private lazy val elements = new HashMap[String, String]

  def getIntOrDefault(key : String, default : Int) = {
    elements getOrElseUpdate(key, default.toString) toInt
  }
}
