package cn.ac.nci.ztb.hs.common

import com.typesafe.config.{ConfigException, ConfigFactory}
import org.slf4j.LoggerFactory

/**
  * Created by Young on 16-8-30.
  */
object Configuration {

  private val config = ConfigFactory.load

  private val logger = LoggerFactory getLogger getClass

  def getIntOrDefault(key : String, default : Int) = getOrDefault(key, default).toInt

  def apply(key: String) = {
    logger debug s"尝试获取配置项$key"
    config getString key
  }

  def getInt(key: String) = Configuration(key).toInt

  def getOrDefault(key: String, default: Any) =
    try {
      Configuration(key)
    } catch {
      case _: ConfigException.Missing => default.toString
    }


  def main(args: Array[String]): Unit = {
    println(Configuration("h.master.port"))
    println(Configuration("h.master.host"))
  }
}
