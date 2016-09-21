package cn.ac.nci.ztb.hs.io

/**
  * Created by Young on 16-9-18.
  */
trait ComparableWritable[T] extends Writable with Comparable[T] {
  def-(o : T) : T

  def+(o : T) : T

  def>(o : T) : Boolean

  def<(o : T) : Boolean
}
