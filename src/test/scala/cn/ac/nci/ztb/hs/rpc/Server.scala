package cn.ac.nci.ztb.hs.rpc

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.io.IntegerWritable
import org.junit.{Assert, Before, Test}

import scala.language.postfixOps

/**
  * Created by Young on 16-9-5.
  */
class RPCTest {

  var server : Server = _
  var cal : Protocol = _
  @Before
  def startServer {
    server = RPC.getServer(8889, "localhost")
    server init;
    server start;
    server.addProtocolAndInstance(classOf[Protocol], new ProtocolImpl)
    cal = RPC.getProxy(classOf[Protocol], new InetSocketAddress("localhost", 8889))
  }

  @Test
  def calculate {
    val valueL = 123
    val valueR = 234

    Assert assertEquals(cal add(new IntegerWritable(valueL),
      new IntegerWritable(valueR)) getValue, valueL+valueR)
  }
}

trait Protocol {
  def add(value1 : IntegerWritable, value2 : IntegerWritable) : IntegerWritable
}

class ProtocolImpl extends Protocol{
  override def add(value1: IntegerWritable, value2: IntegerWritable) =
    value1 + value2
}