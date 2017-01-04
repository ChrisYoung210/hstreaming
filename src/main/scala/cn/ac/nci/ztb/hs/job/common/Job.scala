package cn.ac.nci.ztb.hs.job.common

import java.util.concurrent.atomic.AtomicInteger

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * @author Young
  * @version 1.0
  * 对异构流计算作业的描述，包含可执行Jar包路径，节点信息，上游任务等信息
  * 该类中的方法是线程不安全的。
  */
class Job {

  //val jobName = s"HStreamingJob${new Date(System.currentTimeMillis)}"

  private val NEXT_TASK_ID = new AtomicInteger

  val tasks = new ArrayBuffer[Task]

  val tasksID = new mutable.HashMap[Int, Task]

  val isolateTask = new mutable.HashSet[Task]

  /**
    * 加入流计算节点拓扑图的起点，可以有多个节点。
    * @param task 流拓扑结构的起始节点
    * @return
    */
  def register(task: Task) = {
    if (isolateTask contains task) isolateTask -= task
    else task setTaskID NEXT_TASK_ID.getAndIncrement

    task addUpstreamTask -1
    tasks += task
    this
  }

  def register(task: Task, preTask: Task) = {

    //判断preTask是否被加入过Job孤立点（包括直接加入以及以孤立点的方式加入）。
    if (preTask.taskId == -1) {
      isolateTask += preTask
    }
    //判断task是否曾被以孤立点的形式加入过Job
    if (isolateTask contains task) {
      isolateTask -= task
    }
    //task setUpstreamTask preTask.getTaskId
    tasks += task
    this
  }

}
