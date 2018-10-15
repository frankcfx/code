package com.cfx.learningproject.scala.monoid

trait SemiGroup[T] {
  def append(a: T, b: T): T
}
