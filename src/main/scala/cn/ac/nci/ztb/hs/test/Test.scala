package cn.ac.nci.ztb.hs.test

import java.io.ByteArrayOutputStream

import com.esotericsoftware.kryo.Kryo
import com.esotericsoftware.kryo.io.{Input, Output}
import org.objenesis.strategy.SerializingInstantiatorStrategy

/**
  * @author Young
  * @version 1.0
  *          CreateTime: 16-11-8 下午4:52
  */
object Test {

  def serilizable(kryo: Kryo, obj: AnyRef): Array[Byte] = {
    val stream = new ByteArrayOutputStream
    val out = new Output(stream)
    kryo writeObject(out, obj)
    out flush()
    stream.toByteArray
  }

  def deserilizable(kryo: Kryo, arr: Array[Byte]): AnyRef = {
    val in = new Input(arr)
    kryo.readObject(in, classOf[ABC])
  }

  def main(args: Array[String]): Unit = {
    val kryo = new Kryo()
    kryo setInstantiatorStrategy new SerializingInstantiatorStrategy
    kryo.register(classOf[ABC])

    val s = serilizable(kryo, new ABC)
    println(deserilizable(kryo, s).asInstanceOf[ABC])
  }
}

class ABC extends Serializable {
  val number = TestEnum.FIRST

  override def toString: String = number.toString
}

object TestEnum extends Enumeration {
  type TestEnum = Value
  val FIRST, SECOND = Value
}