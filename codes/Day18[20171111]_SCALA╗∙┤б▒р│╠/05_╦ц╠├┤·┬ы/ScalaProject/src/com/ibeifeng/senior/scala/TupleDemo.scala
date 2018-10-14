package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/11.
  */
object TupleDemo {
  def main(args: Array[String]): Unit = {
    // 一元组
    val tuple1 = Tuple1.apply(12)
    println(tuple1)

    // 二元组
    val t2 = ("gerry", 18)
    val name1 = t2._1
    val age1 = t2._2
    println(s"name=${name1}, age=${age1}, class=${t2.getClass.getName}")

    // 嵌套的元组
    val tt: ((String, String, String), Int) = (("gerry", "xiaoming", "xiaoliu"), 18)
    println(tt._1._2 + ":" + tt._2)

    // 直接进行数据的赋值
    val (name, age) = t2
    println(s"name=${name}, age=${age}")


    val t22 = (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2)
    //    val t23 = (1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3)


    // 不可以进行赋值操作
    val tt3 = (Array(1, 2, 3), 12)
//    tt3._2 = 200 // 重新赋值，不允许
//    tt3._1 = Array(123, 456) // 重新复制，不允许
    tt3._1(0) = 10000 // 这个允许的


    // 元组表示
    val tuple5: (String, Int, String) = ("gerry", 18, "131xxxxxx")
    val name3: String = tuple5._1
    val age3: Int = tuple5._2
    val phone3: String = tuple5._3
    println(s"name=${name3}, age=${age3}, phone=${phone3}")
  }
}
