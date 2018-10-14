package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/11.
  */
object WhileDemo {
  def main(args: Array[String]): Unit = {
    val arr = (3 to 10).toArray
    val length = arr.length
    var i = 0
    while (i < length) {
      // 数组的元素获取使用小括号，而不是方括号
      println(s"下标为${i}的对应元素的值为:${arr(i)}")
      i += 1
    }
    println(s"end......${i}")
  }
}
