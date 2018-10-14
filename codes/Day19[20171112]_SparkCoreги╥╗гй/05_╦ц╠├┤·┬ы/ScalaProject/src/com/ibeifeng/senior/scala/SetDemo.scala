package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/12.
  */
object SetDemo {
  def main(args: Array[String]): Unit = {
    // 不可变Set
    val set = Set(1, 2, 3, 4, 2, 1)
    import scala.collection.immutable
    val set1 = immutable.Set(4, 5, 6, 7)

    // 添加元素形成新集合，对老集合没有影响
    val set2 = set + 55

    // 数据处理API和List基本一样
    set2.map(v => v * 2)
    set2.map(v => v % 2) // 稍微注意下：Set集合是会讲重复数据进行删除的

    // 数据访问：因为set是一个无序的集合，没有基于索引进行数据访问，只能通过高阶API对set集合中的数据进行操作
    // 可以判断数据是否存在
    val flag: Boolean = set(2)
    println(s"元素2在集合set中是否存在：${flag}")

    // 不可变set
    import scala.collection.mutable
    val buf1 = mutable.Set[Int]()

    // 增加元素
    buf1 += 12
    buf1 += (12, 12, 23, 34)
    buf1 ++= List(1, 2, 3, 4, 5, 6, 7, 8)

    // 删除元素
    buf1 -= 12
    buf1 -= 12
    buf1 -= (23, 24)
    buf1 --= Array(1, 2, 3)

    // 基于判断的规则进行一个删除、增加操作
    buf1(23) = true // 增加元素
    buf1(8) = false // 元素删除

  }
}
