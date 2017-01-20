package cn.ac.nci.ztb.hs.job.common

import java.util.concurrent.atomic.AtomicInteger

import cn.ac.nci.ztb.hs.exception.ExistDAGInStreamException

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * @author Young
  * @version 1.0
  * 对异构流计算作业的描述，包含可执行Jar包路径，节点信息，上游任务等信息
  * 该类中的方法是线程不安全的。
  */
class Job {

  private val NEXT_TASK_ID = new AtomicInteger

  val tasks = new ArrayBuffer[Task]

  val idToTask = new mutable.HashMap[Int, Task]

  /**
    * 向Job中注册一个Task，使用该方法注册Task时，Task将被设置为起始节点
    * @param task
    * @return
    */
  def register(task: Task*): Job = {
    task foreach allotID
    this
  }

  def registerWithPreTask(task: Task, preTask: Task): Job = {
    registerWithPreTasks(task, Seq(preTask): _*)
  }

  def registerWithPreTasks(task: Task, preTasks: Task*): Job = {
    preTasks foreach allotID
    allotID(task)
    task addUpstreamTask (preTasks.map(_.taskId): _*)
    this
  }

  private def allotID(task: Task): Unit = {
    if (task.taskId == -1) {
      task setTaskID NEXT_TASK_ID.getAndIncrement
      task addUpstreamTask -1
      tasks += task
      idToTask += task.taskId -> task
    }
  }

  def check {

    //判断是否存在环
    val traversaledTask = new mutable.HashSet[Task]

    def dfs(task: Task) {
      if (task != null)
        if (traversaledTask contains task) throw new ExistDAGInStreamException
        else {
          traversaledTask += task
          task.upstream foreach(x => idToTask.get(x).foreach(dfs))
          traversaledTask -= task
        }
    }

    tasks foreach {
      x => dfs(x)
    }
  }

  override def toString: String = tasks mkString "\n"

}

object Job {
  def main(args: Array[String]): Unit = {
    val a = new Task("1")
    val b = new Task("2")
    val c = new Task("3")

    val job = new Job
    job.register(a)
    job.registerWithPreTask(b, a)
    job.registerWithPreTask(c, b)

    println(job)

    job.check
  }
}