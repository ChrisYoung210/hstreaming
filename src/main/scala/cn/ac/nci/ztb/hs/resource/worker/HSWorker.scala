package cn.ac.nci.ztb.hs.resource.worker

import org.slf4j.LoggerFactory

/**
  * @author Young
  * @version 1.0
  * CreateTime: 16-10-13 上午10:51
  */
object HSWorker {
  private val logger = LoggerFactory getLogger HSWorker.getClass

  def main(args: Array[String]): Unit = {
    WorkerMonitor.init
    WorkerMonitor.start
  }
}
