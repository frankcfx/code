package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/11.
  */
object ForDemo {
  def main(args: Array[String]): Unit = {
    val arr = (3 to 10).toArray

    // 基本的for循环
    println("基本的for循环=======================")
    for (item <- arr) {
      print(s"${item}\t")
    }
    println()

    // for循环的守护模式
    println("守护模式==================")
    for (item <- arr) {
      if (item % 2 != 0) {
        print(s"${item}\t")
      }
    }
    println()
    for (item <- arr if item % 2 != 0) {
      print(s"${item}\t")
    }
    println()

    // 嵌套循环
    println("9*9乘法表===================")
    for (i <- 1 to 9) {
      for (j <- 1 to 9) {
        if (i >= j) {
          print(s"${j} * ${i} = ${i * j} ")
        }
      }
      println()
    }
    println()

    for (i <- 1 to 9) {
      for (j <- 1 to 9 if i >= j) {
        print(s"${j} * ${i} = ${i * j} ")
      }
      println()
    }
    println()

    for (i <- 1 to 9) {
      for (j <- 1 to i) {
        print(s"${j} * ${i} = ${i * j} ")
      }
      println()
    }
    println()


    for (i <- 1 to 9; j <- 1 to i) {
      print(s"${j} * ${i} = ${i * j} ")
      if (i == j) println()
    }
    println()

    for {
      i <- 1 to 9 // 第一层循环
      j <- 1 to i // 第二层循环
    } {
      print(s"${j} * ${i} = ${i * j} ")
      if (i == j) println()
    }
    println()

    //基于原有的集合构建一个新的集合 ==> 使用yield关键字，形成的集合类型和第一层的循环的集合类型的是一样的
    val result: Array[Int] = {
      for {
        i <- arr
        j <- 1 to 3
      } yield {
        i * j
      }
    }
    println(result.mkString(";"))

  }
}
