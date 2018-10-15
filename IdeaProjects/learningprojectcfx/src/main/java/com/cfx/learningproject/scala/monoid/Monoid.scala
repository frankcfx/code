package com.cfx.learningproject.scala.monoid

trait Monoid[T] extends SemiGroup[T] {
  // 定义单位元
  def zero: T
}
