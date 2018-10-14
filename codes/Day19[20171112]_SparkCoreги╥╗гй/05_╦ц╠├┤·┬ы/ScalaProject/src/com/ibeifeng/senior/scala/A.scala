package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/12.
  */
trait A {
  def f(): Unit = {
    println("A f method")
  }

  def g(): Unit
}

trait B extends A

trait C

trait D extends A with B with C

abstract class C1 extends D

class C2 extends C1 with B with A  with C with D {
  override def g(): Unit = println("nihao")
}

object C2 {
  def main(args: Array[String]): Unit = {
    val c2 = new C2()
    c2.g()
  }

}

