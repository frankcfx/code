package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/12.
  */
class Student private() {
  var name: String = _
  var age: Int = _

  def apply(): String = s"${name}:${age}"

  def apply(name: String): String = {
    if (name == this.name) s"${name}:${age}"
    else s"unkonwn:${age}"
  }

  def apply(name: String, age: Int): String = s"${name}:${age}"

  def update(name: String, age: Int): Unit = {
    if (this.name == name) {
      this.age = age
    }
  }

  def update(name: String, flag: Boolean, age: Int): Unit = {
    if (this.name == name) {
      this.age = age
    } else if (flag) {
      this.age += age
    }
  }

  def this(name: String) {
    this()
    this.name = name
  }

  override def toString = s"Student($name, $age)"
}

object Student {
  def apply(): Student = new Student()

  def apply(name: String, age: Int): Student = {
    val t = new Student()
    t.name = name
    t.age = age
    t
  }
}

object Student2 {
  def apply(name: String): Student = new Student(name)
}

object StudentDemo {
  def main(args: Array[String]): Unit = {
    val stuent = Student()
    println(stuent)
    val s2 = Student("gerry", 12)
    println(s2)
    println(Student2("gerry"))
    println(Person("gerry"))

    println(s2())
    println(s2("gerry"))
    println(s2("tom"))
    println(s2("xiaoming", 12))

    s2("gerry") = 123
    println(s2)
    s2("tom") = 12
    println(s2)
    s2("gerry", true) = 123
    println(s2)
    s2("tom", true) = 12
    println(s2)

  }
}