package cn.ac.nci.ztb.hs.utils

import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

import scala.language.postfixOps

/**
  * Created by Young on 16-9-1.
  */
object Reflections {

  private lazy val CONSTRUCTOR_CACHE = new ConcurrentHashMap[Class[Any], Constructor[_]]

  def newInstance[T](clazz : Class[T]) :T= {
    //val constructor : Constructor[T] = {CONSTRUCTOR_CACHE get clazz}
    /*if (constructor == null) {
      constructor = clazz getDeclaredConstructor()
      constructor setAccessible true
      CONSTRUCTOR_CACHE put(clazz, constructor)
    }
    constructor newInstance()*/
    val constructor = clazz getDeclaredConstructor()
    constructor setAccessible true
    constructor newInstance()
  }

/*  def newInstance[T : ClassTag] = {
    val clazz = classTag[T].runtimeClass
    var constructor = CONSTRUCTOR_CACHE get clazz
    if (constructor == null) {
      constructor = clazz getDeclaredConstructor()
      constructor setAccessible true
      CONSTRUCTOR_CACHE put(clazz, constructor)
    }
    constructor newInstance() asInstanceOf[T]
  }*/
}
