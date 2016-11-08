package cn.ac.nci.ztb.hs.resource.worker

import java.util.{Timer, TimerTask}

import cn.ac.nci.ztb.hs.common.Configuration

/**
  * @author Young
  * @version 1.0
  *          CreateTime: 16-10-13 上午10:57
  */
private[worker] abstract class ResourceDetector {

  val detectPeriod = Configuration.getOrDefault("resource.detect.period", 3000).toLong

  val timer = new Timer()

  var interrupt = false

  def setInterrupt() { interrupt = true }

  def getDetectTask: TimerTask

  def startContinuousDetect {
    timer.schedule(getDetectTask, detectPeriod)
  }
}
