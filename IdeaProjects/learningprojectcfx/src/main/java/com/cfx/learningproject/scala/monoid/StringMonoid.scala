package com.cfx.learningproject.scala.monoid

object StringMonoid extends Monoid[String] {
  def append(a: String, b: String): String = a + b
  def zero: String = ""
}
