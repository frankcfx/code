package com.ibeifeng.senior.scala

import scala.collection.immutable.Iterable

/**
  * Created by ibf on 11/12.
  */
object MapDemo {
  def main(args: Array[String]): Unit = {
    // Map是一个存储Key/Value键值对数据的集合，并且要求Key唯一；Scala中使用二元组来表示Key/Value键值对，其中二元组的第一个元素认为是Key，第二个元素认为是Value
    // key -> value：其实中间的->是一个函数，函数的作用就是构建一个Key/Value键值对的二元组
    val t: (String, Int) = "gerry" -> 12
    "gerry".->(12)
    // 不可变Map
    val map1 = Map(("abc", 123), ("def", 456), ("gerry", 123))
    val map2 = Map("abc" -> 123, "def" -> 456, "gerry" -> 123)

    // 数据读取: 数据的读取基于给定key的方式，当key不存在的时候，抛出异常java.util.NoSuchElementException: key not found: abcd
    println(s"abc=${map1("abc")}")

    // 在获取数据前判断key是否存在
    if (map1.contains("abc")) map1("abc") else -1
    // 数据读取：数据存在就返回，如果不存在，就返回默认值
    println(s"abc=${map1.getOrElse("abc", -1)}")
    println(s"abcd=${map1.getOrElse("abcd", -1)}")
    //    println(s"abcd=${map1.getOrElse("abcd", throw new RuntimeException)}")

    // 可变Map
    import scala.collection.mutable
    val map3 = mutable.Map[String, Int]()

    // 增加元素
    map3 += "abc" -> 2
    map3 += (("def", 1))
    map3 += (("def", 1), ("gerry", 2))
    map3 ++= map1

    // 删除 ==> 基于key删除元素
    map3 -= "abc"
    map3 -= ("abc", "gerry")

    // 数据更新（Insert or update）
    map3("gerry") = 123456

    // 循环语句
    val t1 = ("gerry", 16)
    val (name, age) = t1
    for (item <- map1) {
      println(s"key=${item._1}, value=${item._2}")
    }
    for ((key, value) <- map1) {
      println(s"key=${key}, value=${value}")
    }
    for ((key, _) <- map1) {
      println(s"key=${key}")
    }
    for ((_, value) <- map1) {
      println(s"value=${value}")
    }

    // Map集合的API基本和List一样
    val result1: Iterable[String] = map1.map(t => {
      val key = t._1
      val value = t._2

      key + ":" + value
    })

    map1.map {
      case (key, value) => {
        key + ":" + value
      }
    }
    map1.toList.map {
      case (key, value) => {
        key + ":" + value
      }
    }

  }
}
