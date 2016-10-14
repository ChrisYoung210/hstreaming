package cn.ac.nci.ztb.hs.resource.common

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.resource.common.NodeAction.NodeAction

/**
  * Created by Young on 16-9-19.
  */
trait WorkerTracker {

  /**
    * 用于接受Worker的注册信息，在验证可以注册后，向registerWorkers添加新节点的WorkerState
    * 并分配返回WorkerId
    * @param launcherHost Worker的Launcher模块RPC绑定的IP
    * @param launcherPort Worker的Launcher模块RPC绑定的端口
    * @param originalResource Worker启动后的资源总量
    * @return Master在确认注册信息后会生成一个WorkerId并将其返回给注册的Worker
    */
  def registerWorker(launcherHost: String,
                     launcherPort: Integer,
                     originalResource: Resource): WorkerId

  /**
    * 当Worker向Master注册成功后，定时向Master发送心跳。
    * @param workerId Worker向Master注册时，Master分配的WorkerId
    * @param state  Worker当前的健康状况
    * @param remainingResource  Worker当前资源剩余量
    * @return Master通知Worker下一步行为
    */
  def workerHeartbeat(workerId: WorkerId,
                      state: WorkerHealth,
                      remainingResource: Resource): NodeAction
}
