package com.cfx.learningproject.scalaImplicit

import java.io.File

import scala.io.Source

class Fraction(n: Int, d: Int) {
  val num: Int = n
  val den: Int = d
  //使用操作符的名称来定义一个方法
  def *(other: Fraction): Fraction = new Fraction(num* other.num, den* other.den)

  override def toString: String = this.n.toString + this.d.toString
}

class RichFile(val from: File) {
  def read = Source.fromFile(from.getPath).mkString
}

object FractionTest {
  def main(args: Array[String]): Unit = {

    val fraction = new Fraction(4,5)
    val fraction2 = new Fraction(3,4)

//    println(fraction * fraction2)


    implicit def int2Fraction(n: Int) = new Fraction(n, 1)

    val result = 3 * fraction
//    println(result)


    implicit def file2RichFile(file:File) = new RichFile(file)
    val contents = new File("/home/cfx/IdeaProjects/learningprojectcfx/src/main/java/com/cfx/learningproject/scalaImplicit/FractionTest.scala").read

//    println(contents)

    def smaller[T](a:T ,b: T)(implicit order: T => Ordered[T]) = {
      if(a > b) a else b
    }

    val a = 2
    val b = 4
    println(smaller(a,b))

    import Predef.intWrapper


  }
}