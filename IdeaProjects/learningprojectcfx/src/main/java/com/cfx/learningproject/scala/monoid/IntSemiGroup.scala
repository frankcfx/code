package com.cfx.learningproject.scala.monoid

object IntSemiGroup extends SemiGroup[Int] {
  def append(a: Int, b: Int): Int = a + b
}

