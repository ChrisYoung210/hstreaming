package cn.ac.nci.ztb.hs.io

/**
  * Created by young on 16-9-14.
  */
case class IntegerWritable(var value : Int) extends ComparableWritable[IntegerWritable]{
  def getValue = value

  override def toString: String = value + ""

  override def compareTo(o: IntegerWritable): Int = value compare o.value

  override def +(addValue : IntegerWritable) = copy(value + addValue.value)

  override def -(o: IntegerWritable): IntegerWritable = copy(value-o.value)

  override def >(o: IntegerWritable): Boolean = value > o.value

  override def <(o: IntegerWritable): Boolean = value < o.value

  override def equals(o: Any) = o match {
    case null => false
    case o1: IntegerWritable => value == o1.value
    case o1: Int => value == o1
    case _ => false
  }

  override def hashCode = value hashCode()

  def <(o: Int): Boolean = value < o
}
