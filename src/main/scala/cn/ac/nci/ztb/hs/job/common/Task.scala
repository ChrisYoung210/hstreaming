package cn.ac.nci.ztb.hs.job
package common

import cn.ac.nci.ztb.hs.resource.common.WorkerId

import scala.collection.mutable

/**
  * @author Young
  * @version 1.0
  */
class Task(exePath: String,
           blackList: Array[WorkerId],
           writeList: Array[WorkerId]) {

  private val upstreamTaskIds = new mutable.HashSet[Int]

  private var taskID: Int = 0

  private[job] def addUpstreamTask(upstreamTaskIds: Int*) {
    upstreamTaskIds foreach (this.upstreamTaskIds += _)
  }

  private[job] def addUpstreamTask(upstreamTasks: Task*) {
    upstreamTasks foreach (upstreamTaskIds += _.taskId)
  }

  def this(exePath: String) = this(exePath, null, null)

  private[common] def setTaskID(taskId: Int) { this.taskID = taskId }

  def taskId = taskID

  def checkout = {
    if (upstreamTaskIds.isEmpty) false
    else if (upstreamTaskIds.size == 1) true
    else if (upstreamTaskIds contains -1) false
  }

}

object Task {

  val JAVA_RUNNABLE_PACKAGE = 1

  val WEB_SERVICE = 2

  val SYSTEM_SERVICE = 3

}
