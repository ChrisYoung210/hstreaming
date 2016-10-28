package cn.ac.nci.ztb.hs.resource.master

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.common.{Configuration, Service}
import cn.ac.nci.ztb.hs.resource.common.NodeAction.NodeAction
import cn.ac.nci.ztb.hs.resource.common._
import cn.ac.nci.ztb.hs.resource.master.scheduler.WorkerState
import cn.ac.nci.ztb.hs.rpc.RPC
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.language.postfixOps

/**
  * @author Young
  * @version 1.0 Create time 16-9-18
  * object WorkersManager用于管理worker，包括作为Master-Worker协议的RPC Server，
  * 并提供所有注册至Server的Worker信息等功能。
  */
private[master] object WorkersManager extends Service {

  private val logger = LoggerFactory getLogger getClass

  private val registerWorkers = new mutable.HashMap[WorkerId, WorkerState]()

  private val server = RPC.getServer(Configuration getIntOrDefault("master.port", 8888))

  override def init = {
    this.synchronized {
      state match {
        case State.UNINITED =>
          server init;
          server addProtocolAndInstance(classOf[WorkerTracker],
            new WorkerTracker {

              /**
                * 当Worker向Master注册成功后，定时向Master发送心跳。
                *
                * @param workerId          Worker向Master注册时，Master分配的WorkerId
                * @param state             Worker当前的健康状况
                * @param remainingResource Worker当前资源剩余量
                * @return Master通知Worker下一步行为
                */
              override def workerHeartbeat(workerId: WorkerId,
                                           state: WorkerHealth,
                                           remainingResource: Resource): NodeAction = {
                val worker = registerWorkers(workerId)
                if (worker == null) NodeAction.SHUTDOWN
                else {
                  worker updateRemainingResource remainingResource
                  NodeAction.NORMAL
                }
              }

              /**
                * 用于接受Worker的注册信息，在验证可以注册后，向registerWorkers添加新节点的WorkerState
                * 并分配返回WorkerId
                *
                * @param launcherHost     Worker的Launcher模块RPC绑定的IP
                * @param launcherPort     Worker的Launcher模块RPC绑定的端口
                * @param originalResource Worker启动后的资源总量
                * @return Master在确认注册信息后会生成一个WorkerId并将其返回给注册的Worker
                */
              override def registerWorker(launcherHost: String,
                                          launcherPort: Integer,
                                          originalResource: Resource): WorkerId = {
                val workerState = new WorkerState(new InetSocketAddress(launcherHost, launcherPort),
                  originalResource)
                registerWorkers += ((workerState.workerId, workerState))
                logger info s"接收到Worker（$launcherHost）的注册信息，为其分配WorkerID=$WorkerId。"
                workerState.workerId
              }
            })
        case State.STOPED =>
          val e = new IllegalStateException("The WorkersManager cannot be re-initial," +
            "because it has been shutdown.")
          logger error(e.getMessage, e)
          throw e
        case _ =>
          logger warn s"Expect initial WorkersManager, but current state is $state."
      }
      state = State.INITED
    }
    this
  }

  override def stop = {
    synchronized {
      server stop;
      state = State.STOPED
    }
    this
  }

  override def start = {
    synchronized {
      state match {
        case State.INITED => server start
        case State.STARTED => logger warn "WorkersManager had started."
        case _ =>
          val e = new IllegalStateException(s"The WorkersManager cannot start, because its state is $state")
          logger error(e.getMessage, e)
          throw e
      }
      state = State.STARTED
    }
    this
  }

  override def toString = "Current State: " + state + "\n[\n" +
    registerWorkers.values.map(_.toString).mkString("\n") + "\n]"
}
