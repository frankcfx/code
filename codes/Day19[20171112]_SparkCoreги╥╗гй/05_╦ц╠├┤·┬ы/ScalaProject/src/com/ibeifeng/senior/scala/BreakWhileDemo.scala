package com.ibeifeng.senior.scala

import scala.util.control.Breaks

/**
  * Created by ibf on 11/11.
  */
object BreakWhileDemo {
  def main(args: Array[String]): Unit = {
    val arr = (3 to 10).toArray
    val length = arr.length
    var i = 0
    val loop = new Breaks()

    loop.breakable {
      while (i < length) {
        // 数组的元素获取使用小括号，而不是方括号
        println(s"下标为${i}的对应元素的值为:${arr(i)}")
        i += 1
        if (i > 3) {
          // break跳出
          // 不支持break命令，但是可以通过其它方式模拟出类似的效果
          loop.break()
        }
      }
    }
    println(s"end1......${i}")

    i = 0
    while (i < length) {
      // 数组的元素获取使用小括号，而不是方括号
      println(s"下标为${i}的对应元素的值为:${arr(i)}")
      i += 1
      if (i > 3) {
        // 直接结束当前程序的运行啦
        return "abc"
      }
    }
    println(s"end2......${i}")
  }
}
