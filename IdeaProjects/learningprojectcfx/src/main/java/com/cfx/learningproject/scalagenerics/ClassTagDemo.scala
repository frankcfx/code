package com.cfx.learningproject.scalagenerics

import scala.reflect.ClassTag

//由于Manifest存在缺陷，所以后面推出了ClassTag
//ClassTag是TypeTag的一个弱化的版本，运行时保存类型信息T。
//更多信息参考文档，文档写的非常清楚
class ClassTagDemo[T] {

  def mkArray[T: ClassTag](elems: T*) = Array[T](elems: _*)

  def mkArray2(elems: T*)(implicit x: ClassTag[T]) = Array[T](elems: _*)

  //  mkArray: [T](elems: T*)(implicit evidence$1: scala.reflect.ClassTag[T])Array[T]

  //  scala> mkArray(42, 13)
  //  res0: Array[Int] = Array(42, 13)
  //
  //  scala> mkArray("Japan","Brazil","Germany")
  //  res1: Array[String] = Array(Japan, Brazil, Germany)
//  Fraction

}

object ClassTagDemo extends App {

  val c = new ClassTagDemo[Int]
  c.mkArray(42, 13)
  c.mkArray2(42, 13)

  def sum(implicit x: Int, y: Int) = x + y
  //只能指定一个隐式值
  //例如下面下定义的x会自动对应maxFunc中的
  //参数x,y即x=3,y=3，从而得到的结果是6
  implicit val x:Int=3
  //不能定义两个隐式值
  //implicit val y:Int=4
  println(sum)

  implicit def double2Int(x:Double)=x.toInt
  def f(x:Int)=x
  //方法中输入的参数类型与实际类型不一致，此时会发生隐式转换
  //double类型会转换为Int类型，再进行方法的执行
  f(3.14)

}






