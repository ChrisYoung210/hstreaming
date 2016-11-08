package cn.ac.nci.ztb.hs.test

import cn.ac.nci.ztb.hs.resource.common.WorkerTracker

/**
  * @author Young
  * @version 1.0
  *          CreateTime: 16-11-8 下午4:52
  */
object Test {
  def main(args: Array[String]): Unit = {
    classOf[WorkerTracker].getMethods.foreach(x => println(x.getParameterTypes mkString "\t\t\t"))
  }
}
