package com.cfx.learningproject.scalatypeclass

trait Comparable2[T] {
  def comp(x:T, y:T) = x.hashCode - y.hashCode
}

class A

object A {
  implicit val compA = new Comparable2[A] { override def comp(o:A, b:A) = super.comp(o,b) }
}

object TypeClassTest {

  def main(args: Array[String]): Unit = {

    val x = new A
    val y = new A
    call(x,y)

  }

  def call[T](x:T, y:T) (implicit c : Comparable2[T]) {
    println(c.comp(x,y))
  }

}
