package cn.ac.nci.ztb.hs.resource.master.scheduler

import java.net.InetSocketAddress

import cn.ac.nci.ztb.hs.io.{IntegerWritable, LongWritable}
import cn.ac.nci.ztb.hs.resource.common.{Resource, WorkerId}

import scala.language.postfixOps

/**
  * Created by Young on 16-9-18.
  */
private[resource] class WorkerState(address: InetSocketAddress,
                  totalResource: Resource) {
  val workerId = WorkerId.getNewWorkerId
  private var lastUpdateTimestamp = System.currentTimeMillis
  private var used = new Resource(0l, 0)
  private var remaining = totalResource.copy()

  private def updateTimestamp() { lastUpdateTimestamp = System.currentTimeMillis}

  def getLastUpdateTimestamp = lastUpdateTimestamp

  def getUsedResource = used

  def getRemainingResource = remaining

  def getWorkerId = workerId

  def getAddress = address

  def getTotalResource = totalResource

  def updateRemainingResource(resource: Resource) {
    remaining = resource
    used = totalResource - remaining
    updateTimestamp()
  }

  override def toString = new StringBuilder("[WorkerId: ") append
    workerId append ", LauncherBindAddress: " append address append
    ", TotalResource: " append totalResource append ", UsedResource: " append
    used append ", RemainingResource" append remaining append
    ",UpdateTimestamp: " append new java.util.Date(lastUpdateTimestamp) append "]" toString

}
