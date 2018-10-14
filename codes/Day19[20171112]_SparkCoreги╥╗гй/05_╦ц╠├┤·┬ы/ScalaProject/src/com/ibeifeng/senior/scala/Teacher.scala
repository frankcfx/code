package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/12.
  */
case class Teacher(var name: String, age: Int) {
  override def toString: String = s"${name}:${age}"
}

object Teacher {
  def apply(name: String): Teacher = new Teacher(name, 12)
}