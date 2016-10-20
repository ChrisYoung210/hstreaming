package cn.ac.nci.ztb.hs.resource.master

/**
  * @author Young
  * @version CreateTime: 16-10-13 上午10:08
  */
object HSMaster {
  def main(args: Array[String]): Unit = {
    WorkersManager.init
    WorkersManager.start
  }
}
