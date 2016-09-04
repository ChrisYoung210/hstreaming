package cn.ac.nci.ztb.hs.utils

import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

/**
  * Created by Young on 16-9-1.
  */
object Reflections {

  private lazy val CONSTRUCTOR_CACHE = new ConcurrentHashMap[Class[_], Constructor[_]]

  def newInstance[T](clazz : Class[T]) :T= {
    var constructor : Constructor[_] = {CONSTRUCTOR_CACHE get clazz}
    if (constructor == null) {
      constructor = clazz getDeclaredConstructor()
      constructor setAccessible true
      CONSTRUCTOR_CACHE put(clazz, constructor)
    }
    constructor newInstance() asInstanceOf
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
