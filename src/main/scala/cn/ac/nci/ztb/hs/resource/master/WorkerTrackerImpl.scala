package cn.ac.nci.ztb.hs.resource
package master

import cn.ac.nci.ztb.hs.io.{IntegerWritable, StringWritable}
import cn.ac.nci.ztb.hs.resource.common.NodeAction.NodeAction
import cn.ac.nci.ztb.hs.resource.common._
import cn.ac.nci.ztb.hs.resource.master.scheduler.WorkerState

import scala.collection.mutable

/**
  * Created by Young on 16-9-21.
  */
class WorkerTrackerImpl(registerWorkers: mutable.HashMap[WorkerId, WorkerState])
  extends WorkerTracker {

  val hosts = new mutable.HashSet[String]()

  /**
    * Worker启动后向Master注册其基本信息并获得Master为其分配的WorkerId
    *
    * @param host             Worker绑定的host
    * @param launcherPort     Worker绑定的端口
    * @param originalResource Worker启动后的资源总量
    * @return Master在确认注册信息后会生成一个WorkerId并将其返回给注册的Worker
    */
  override def registerWorker(host: StringWritable,
                              launcherPort: IntegerWritable,
                              originalResource: Resource): WorkerId = {
    val workerState = new WorkerState(host, launcherPort, originalResource)
    registerWorkers += workerState.workerId -> workerState
    workerState.workerId
  }

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
    NodeAction.NORMAL
  }
}
