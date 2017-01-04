package cn.ac.nci.ztb.hs.job
package   common

import cn.ac.nci.ztb.hs.job.master.JobMaster

/**
  * @author Young
  * @version 1.0
  * CreateTime: 16-12-15 下午5:04
  */
trait MasterWorkerProtocol {
  def sendJobMaster(jobMaster: JobMaster): Array[JobsStatus]
}
