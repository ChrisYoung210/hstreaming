package cn.ac.nci.ztb.hs.io

/**
  * Created by young on 16-9-14.
  */
class IntegerWritable(var value : Int) extends Writable{
  def getValue = value

  override def toString: String = value + ""

  def +(addValue : IntegerWritable) = new IntegerWritable(value + addValue.value)
}
