package cn.ac.nci.ztb.hs.resource.master.scheduler

import cn.ac.nci.ztb.hs.io.{IntegerWritable, LongWritable, StringWritable}
import cn.ac.nci.ztb.hs.resource.common.{Resource, WorkerId}

import scala.language.postfixOps

/**
  * Created by Young on 16-9-18.
  */
class WorkerState(host: StringWritable,
                  launcherPort: IntegerWritable,
                  totalResource: Resource) {
  val workerId = WorkerId.getNewWorkerId
  private var lastUpdateTimestamp = System.currentTimeMillis
  private var used = new Resource(new LongWritable(0), new IntegerWritable(0))
  private var remaining = totalResource.copy()

  private def updateTimestamp { lastUpdateTimestamp = System.currentTimeMillis}

  def getLastUpdateTimestamp = lastUpdateTimestamp

  def getUsedResource = used

  def getRemainingResource = remaining

  def getWorkerId = workerId

  def getHost = host

  def getLauncherPort = launcherPort

  def getTotalResource = totalResource

  def updateUsedResource(resource: Resource) { used = resource; updateTimestamp }

  def updateRemainingResource(resource: Resource) { remaining = resource; updateTimestamp}

  override def toString = new StringBuilder("[WorkerId: ") append
      workerId append ", Host: " append host append ", LauncherPort: " append
      launcherPort append ", TotalResource: " append totalResource append
      ", UsedResource: " append used append ", RemainingResource" append remaining append
      ",UpdateTimestamp: " append new java.util.Date(lastUpdateTimestamp) append "]" toString

}
