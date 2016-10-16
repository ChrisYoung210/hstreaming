package cn.ac.nci.ztb.hs.resource.worker

import cn.ac.nci.ztb.hs.common.Service
import cn.ac.nci.ztb.hs.resource.common.Resource
import cn.ac.nci.ztb.hs.resource.worker.ResourceDetectorImpl.detector
import org.slf4j.LoggerFactory

/**
  * @author Young
  * @version 1.0
  * CreateTime: 16-10-13 上午10:56
  */
object WorkerMonitor extends Service {

  private val logger = LoggerFactory getLogger WorkerMonitor.getClass

  private var resourceDetector: ResourceDetector[Resource] = _

  def startDetector(implicit resourceDetector: ResourceDetector[Resource]) = {
    this.resourceDetector = resourceDetector
    new Thread(resourceDetector) start
  }

  override def init: Service = {
    state.synchronized {
      if (state eq State.UNINITED) {
        state = State.INITED
      } else logger error s"由于WorkerMonitor状态错误${getClass}初始化失败。"
    }
    this
  }

  override def start: Service = {
    state.synchronized {
      if (state eq State.INITED) {
        startDetector
        logger info getClass + "已完成启动。"
        state = State.STARTED
        true
      } else {
        logger error "由于Client Service状态错误" + getClass + "启动失败。"
        false
      }
    }
    this
  }

  override def stop: Service = {
    state.synchronized {
      if (state eq State.STARTED) {
        resourceDetector setInterrupt()
        state = State.STOPED
        logger info getClass + "已停止。"
        true
      } else {
        logger error "由于Client Service状态错误" + getClass + "无法停止。"
        false
      }
    }
    this
  }
}
