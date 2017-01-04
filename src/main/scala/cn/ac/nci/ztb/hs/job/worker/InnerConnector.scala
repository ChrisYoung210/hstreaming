package cn.ac.nci.ztb.hs.job.worker

/**
  * @author Young
  * @version 1.0
  * CreateTime: 16-12-15 下午5:35
  */
trait InnerConnector {

  /**
    * 获取准备进行处理的数据，数据源可能为本节点的输入缓冲区，亦可能为上游子任务的输出缓冲区
    */
  def getData()


  def calculate()

  /**
    * 将计算结果写出，可能写至本节点的输出缓冲区，也可能直接写出到下游子任务的输入缓冲区
    */
  def writeData()

}
