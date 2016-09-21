package cn.ac.nci.ztb.hs.resource.master

import cn.ac.nci.ztb.hs.common.{Configuration, Service}
import cn.ac.nci.ztb.hs.io.{IntegerWritable, StringWritable}
import cn.ac.nci.ztb.hs.resource.common.{Resource, WorkerTracker, WorkerId}
import cn.ac.nci.ztb.hs.resource.master.scheduler.WorkerState
import cn.ac.nci.ztb.hs.rpc.RPC
import org.slf4j.LoggerFactory

import scala.collection.mutable
import scala.language.postfixOps

/**
  * Created by Young on 16-9-18.
  */
object WorkersManager extends Service {

  private val logger = LoggerFactory getLogger getClass

  private var state = State.UNINITED

  private val registerWorkers = new mutable.HashMap[WorkerId, WorkerState]()

  private val server = RPC.getServer(Configuration getIntOrDefault("master.rpc.port", 8765))

  override def init = {
    this.synchronized {
      state match {
        case State.UNINITED =>
          server init;
          server addProtocolAndInstance(classOf[WorkerTracker],
            new WorkerTracker {
              /**
                * worker send its information to master after initialization
 *
                * @param host             the host worker bound
                * @param launcherPort     the port worker listened
                * @param originalResource the worker's original resource
                * @return master generate a new WorkerId and pass it to worker after confirm
                */
              override def registerWorker(host: StringWritable,
                                          launcherPort: IntegerWritable,
                                          originalResource: Resource): WorkerId = {
                val workerState = new WorkerState(host, launcherPort, originalResource)
                registerWorkers += ((workerState.workerId, workerState))
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
