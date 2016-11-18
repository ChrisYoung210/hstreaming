package cn.ac.nci.ztb.hs.rpc

import java.lang.reflect.Method

/**
  * Created by Young on 16-11-13.
  */
object ImplicityInstance {

  implicit val methodOrder = new Ordering[Method] {
    override def compare(x: Method, y: Method): Int = {
      x.getName.compareTo(y.getName) match {
        case 0 => {
          x.getParameterTypes.lengthCompare(y.getParameterTypes.length) match {
            case 0 => var result = 0
              for (i <- x.getParameterTypes.indices) {
                if (result == 0) {
                  result = x.getParameterTypes.apply(i).getName.compareTo(y.getParameterTypes.apply(i).getName)
                }
              }
              result
            case x: Int => x
          }
        }
        case x: Int => x
      }
    }
  }

  def main(args: Array[String]): Unit = {
    classOf[TestMethodOrdering].getMethods.sorted.foreach(println)
  }

}

trait TestMethodOrdering {
  def a(x:Int): Int
  def a(x:Int, y:Long): Long
  def a(x:Long, y:Long): Double
  def a(x:String, y:Char): String
}
