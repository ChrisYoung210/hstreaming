package cn.ac.nci.ztb.hs.utils

import java.util.concurrent.TimeoutException

import org.slf4j.LoggerFactory

import scala.collection.mutable.HashMap
import scala.language.postfixOps

/**
  * Created by Young on 16-9-5.
  */
class BlockingHashMap[K, V](var timeoutMillis : Long) {

  class AsyncResult {
    var element : V = _
    private var available = false

    def get : V = {
      synchronized {
        if (available) element
        else {
          wait(timeoutMillis)
          if (available) element
          else throw new TimeoutException
        }
      }
    }

    def set(value : V) {
      synchronized {
        element = value
        available = true
        notify()
        this
      }
    }
  }



  val elements = new HashMap[K, AsyncResult]

  def get(key : K) = {
    BlockingHashMap.logger debug "prepare get value"
    val result = elements getOrElseUpdate(key, new AsyncResult)
    val value = result get;
    elements.remove(key)
    value
  }

  def put(key : K, value : V) {
    elements getOrElseUpdate(key, new AsyncResult) set value
    BlockingHashMap.logger debug "put value successful." + key
  }

}

object BlockingHashMap {
  private val logger = LoggerFactory getLogger classOf[BlockingHashMap[Any, Any]]
}
