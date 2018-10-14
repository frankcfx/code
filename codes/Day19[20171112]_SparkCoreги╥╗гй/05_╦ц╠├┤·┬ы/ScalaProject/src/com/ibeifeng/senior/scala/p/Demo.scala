package com.ibeifeng.senior.scala.p

import com.ibeifeng.senior.scala.Person

/**
  * Created by ibf on 11/12.
  */
object Demo {
  def main(args: Array[String]): Unit = {
    val person = new Person()
    // 因为v1和v2在这里不能访问
    println(s"person对象值:${person.v3}")
  }
}
