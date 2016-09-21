package cn.ac.nci.ztb.hs.io

/**
  * Created by Young on 16-9-18.
  */
case class LongWritable(var value : Long) extends ComparableWritable[LongWritable] {
  def getValue = value

  override def toString: String = value + ""

  override def compareTo(o: LongWritable): Int = value compare o.value

  override def -(o : LongWritable): LongWritable = copy(value - o.value)

  override def +(addValue : LongWritable) = copy(value + addValue.value)

  override def >(o: LongWritable): Boolean = value > o.value

  override def <(o: LongWritable): Boolean = value < o.value

  override def equals(o: Any): Boolean = o match {
    case null => false
    case o1: LongWritable => value == o1.value
    case o1: Long => value == o1
    case _ => false
  }

  override def hashCode() = value hashCode

  def <(o: Long) : Boolean = value < o
}
