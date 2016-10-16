package cn.ac.nci.ztb.hs.resource.common

import cn.ac.nci.ztb.hs.io.{ComparableWritable, IntegerWritable, LongWritable}
import org.slf4j.LoggerFactory

/**
  * Created by Young on 16-9-18.
  */
case class Resource(memory : Long, virtual_cpu : Int)
  extends ComparableWritable[Resource] {

  def getMemory = memory

  def getVirtualCPU = virtual_cpu

  override def compareTo(o: Resource): Int =
    if (virtual_cpu equals o.virtual_cpu) memory compareTo o.memory
    else virtual_cpu compareTo o.virtual_cpu

  override def -(o : Resource) : Resource = {
    val result = this - o
    if (result.memory < 0) {
      val e =  new IllegalStateException("The residual of Memory cannot be negative.")
      Resource.logger warn(e.getMessage, e)
      throw e
    }
    if (result.virtual_cpu < 0) {
      val e =  new IllegalStateException("The residual of Virtual_CPU cannot be negative.")
      Resource.logger warn(e.getMessage, e)
      throw e
    }
    result
  }

  override def +(o: Resource): Resource = copy(memory+o.memory, virtual_cpu+o.virtual_cpu)

  override def >(o: Resource): Boolean = compareTo(o) > 0

  override def <(o: Resource): Boolean = compareTo(o) < 0

  override def equals(o: Any) = o match {
    case null => false
    case o1: Resource => compareTo(o1) == 0
    case _ => false
  }

  override def hashCode = virtual_cpu.hashCode + memory.hashCode

  override def toString = s"[memory: $translate, virtual_cpu_number: $virtual_cpu.]"

  private def translate : String = {
    var currentMemory = memory
    for(i <- Resource.sizeUnit.indices) {
      if (Resource.sizeUnit.length == i+1 || currentMemory < 1024)
        return currentMemory + Resource.sizeUnit(i)
      else
        currentMemory /= 1024
    }
    ""
  }
}

object Resource {
  val sizeUnit = Array("B", "KB", "MB", "GB", "TB", "PB")

  val logger = LoggerFactory getLogger classOf[Resource]
}