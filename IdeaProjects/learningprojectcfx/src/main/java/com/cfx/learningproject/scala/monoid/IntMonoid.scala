package com.cfx.learningproject.scala.monoid

object IntMonoid extends Monoid[Int] {
  // 二元操作
  def append(a: Int, b: Int) = a + b
  // 单位元
  def zero = 0
}
