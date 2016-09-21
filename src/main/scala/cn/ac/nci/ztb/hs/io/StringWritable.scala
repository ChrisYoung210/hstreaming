package cn.ac.nci.ztb.hs.io

/**
  * Created by Young on 16-9-18.
  */
case class StringWritable(value: String) extends Writable{
  override def toString = value
}
