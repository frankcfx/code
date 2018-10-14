package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/11.
  */
object IFELSEDemo {
  def main(args: Array[String]): Unit = {
    var source = 10

    if (source > 5) {
      println(s"abc:${source} > 5")
    }

    if (source > 10) {
      println(s"2abc:${source} > 10")
    } else {
      println(s"def:${source} <= 10")
    }

    if (source > 10) {
      println(s"3abc:${source}")
    } else if (source > 5) {
      println(s"3def:${source}")
    } else {
      println(s"tgb:${source}")
    }

    // SCALA中不支持三元操作符(?:)，原因：1. 操作符本身是API，三元操作符不好转换为API；2. 其实使用IF-ELSE语句即可实现类似的功能
    // condition ? true-value : false-value
    val result: String = {
      if (source >= 6) {
        println(s"及格:${source}")
        "及格"
      } else {
        println(s"不及格:${source}")
        "不及格"
      }
    }
    println(result)
    source = 3
    val result2 =if (source >= 6) "及格" else "不及格"
    println(result2)


    // Scala中没有Void数据类型，Scala使用Unit表示空数据类型/函数返回值为空(没有返回值)
    val printResult: Unit = println(result2)
  }
}
