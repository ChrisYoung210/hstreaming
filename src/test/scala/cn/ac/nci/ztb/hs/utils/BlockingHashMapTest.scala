package cn.ac.nci.ztb.hs.utils

import org.junit.{Assert, Test}

import scala.concurrent.TimeoutException

/**
  * Created by young on 16-9-12.
  */
class BlockingHashMapTest {

  @Test
  def got {
    val map = new BlockingHashMap[Int, Int](500)
    new Thread(new Runnable {
      override def run(): Unit = {
        map put(1, 1)
        Thread.sleep(600)
        map put(2, 2)
      }
    }).start()
    Assert assertEquals(map get 1, 1)
    var timeout = false
    try {
      map get 2
    } catch {
      case e : TimeoutException => timeout = true
    } finally {
      Assert assertTrue timeout
    }
  }
}
