package cn.ac.nci.ztb.hs.resource.master

import cn.ac.nci.ztb.hs.resource.web.Boot

/**
  * @author Young
  * @version CreateTime: 16-10-13 上午10:08
  */
object HSMaster {
  def main(args: Array[String]): Unit = {
    new Thread(Boot).start()
    WorkersManager.init
    WorkersManager.start
  }
}
