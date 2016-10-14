package cn.ac.nci.ztb.hs.resource.master

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.io.{IntegerWritable, LongWritable}
import cn.ac.nci.ztb.hs.resource.common.{Resource, WorkerTracker}
import cn.ac.nci.ztb.hs.rpc.RPC
import org.junit.Assert._
import org.junit.{Before, Test}

/**
  * Created by Young on 16-9-19.
  */
class WorkersManagerTest {

  var resourceTracker : WorkerTracker = _

  @Before
  def initAndStart() {
    WorkersManager.init
    WorkersManager.start
    resourceTracker = RPC.getProxy(classOf[WorkerTracker],
      new InetSocketAddress("localhost", 8765))
  }

  @Test
  def test() {
    val id = resourceTracker.registerWorker("192.168.12.113", 8787,
      new Resource(LongWritable(128849011888l), IntegerWritable(8)))
    assertEquals(id.getId, 0)
    println(WorkersManager)
  }

}
