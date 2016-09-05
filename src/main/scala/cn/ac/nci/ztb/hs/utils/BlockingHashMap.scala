package cn.ac.nci.ztb.hs.utils

import java.util.concurrent.TimeoutException

import scala.collection.mutable.HashMap

/**
  * Created by Young on 16-9-5.
  */
class BlockingHashMap[K, V](var timeoutMillis : Long) {

  private class AsyncResult {
    private var element : V = ???;
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
    val result = elements getOrElseUpdate(key, new AsyncResult)
    val value = result get;
    elements.remove(key)
    value
  }

  def put(key : K, value : V) {
    elements getOrElseUpdate(key, new AsyncResult) set value
  }

}
