package cn.ac.nci.ztb.hs.resource.worker

import java.net.{InetAddress, InetSocketAddress, NetworkInterface}

import cn.ac.nci.ztb.hs.common.{Configuration, Service}
import cn.ac.nci.ztb.hs.resource.common.{Resource, WorkerTracker}
import cn.ac.nci.ztb.hs.resource.worker.ResourceDetectorImpl.detector
import cn.ac.nci.ztb.hs.rpc.RPC
import org.slf4j.LoggerFactory

/**
  * 该伴生对象运行于Worker节点，用于Worker启动后向Master注册该节点信息，
  * 并且在正常运行时管理、监控Worker端资源状况。
  * @author Young
  * @version 1.0
  * CreateTime: 16-10-13 上午10:56
  */
object WorkerMonitor extends Service {

  private val logger = LoggerFactory getLogger WorkerMonitor.getClass

  //资源探测器，根据实际运行时使用指定的资源探测器
  private var resourceDetector: ResourceDetector[Resource] = _

  private lazy val masterHost = Configuration("master.host")

  private lazy val masterPort = Configuration getInt "master.port"

  private lazy val localIP = InetAddress.getLocalHost.getHostAddress

  private lazy val localPort = Configuration getInt "worker.launcher.port"

  private lazy val workerTracker =
    RPC.getProxy(classOf[WorkerTracker],
      new InetSocketAddress(masterHost, masterPort))

  private val workerId = workerTracker registerWorker(localIP,
    localPort, new Resource(1234567890l, 8))

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
