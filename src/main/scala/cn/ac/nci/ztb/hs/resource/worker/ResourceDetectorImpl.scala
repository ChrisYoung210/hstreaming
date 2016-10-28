package cn.ac.nci.ztb.hs.resource
package worker

import cn.ac.nci.ztb.hs.common.Configuration
import common.Resource

/**
  * @author Young
  * @version 1.0
  * CreateTime: 16-10-13 下午2:26
  */
object ResourceDetectorImpl extends {

  implicit val detector: ResourceDetector[Resource] =
    new ResourceDetector[Resource] {

      override def run(): Unit = {
        while (!interrupt) {
          Thread.sleep(3000)
          lock.synchronized {
            value = Resource(Configuration.getOrDefault("worker.memory", 128849011888l).toLong,
              Configuration.getOrDefault("worker.cpu", 8).toInt)
            timestamp = System.currentTimeMillis
          }
        }
      }
    }
}
