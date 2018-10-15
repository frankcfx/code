package com.cfx.learningproject.scalagenerics

import scala.reflect.ClassTag

class ManifestDemo[T] {

  //可以创建范型数组
  def makeTArray3[T:ClassTag]():Array[T]                = new Array[T](10)

  //Error:(10, 50) cannot find class tag for element type T
//  def makeTArray3[T]():Array[T]                = new Array[T](10)

  def makeTArray34(): Array[String]            = new Array[String](10)

  //Manifest是类型T的显示描述符
  def makeTArray[T: Manifest]()                = new Array[T](10)

  //等效上面的写法
  def makeTArray2()(implicit x: Manifest[T])   = new Array[T](10)

  def makeStringArray(): Array[String]         = new Array[String](10)


}


object ManifestDemo {

  def main(args: Array[String]): Unit = {

    val c = new ManifestDemo[String]

    c.makeTArray()
    c.makeTArray2()

  }

}
