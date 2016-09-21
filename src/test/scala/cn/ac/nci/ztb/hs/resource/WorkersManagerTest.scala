package cn.ac.nci.ztb.hs.resource

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.io.{IntegerWritable, LongWritable, StringWritable}
import cn.ac.nci.ztb.hs.resource.common.{Resource, WorkerTracker}
import cn.ac.nci.ztb.hs.resource.master.WorkersManager
import cn.ac.nci.ztb.hs.rpc.RPC
import org.junit.{Before, Test}
import org.junit.Assert._

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
    val id = resourceTracker.registerWorker(new StringWritable("192.168.12.113"),
      new IntegerWritable(8787),
      new Resource(new LongWritable(128849011888l), new IntegerWritable(8)))
    assertEquals(id.getId, 0)
    println(WorkersManager)
  }

}
