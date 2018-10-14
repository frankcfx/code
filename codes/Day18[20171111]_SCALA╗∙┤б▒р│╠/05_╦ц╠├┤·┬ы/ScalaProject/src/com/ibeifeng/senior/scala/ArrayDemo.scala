package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/11.
  */
object ArrayDemo {
  def main(args: Array[String]): Unit = {
    // 泛型：和java基本一样，区别在于：java中的泛型使用<>来表示，scala使用[]来表示
    // 包的引入：和java基本一样，使用import引入包，区别在于：1. java中的import语句只能出现在package下，class上的这一部分，但是scala中import语句可以出现在任何需要的地方; 2.java中使用*表示引入所有子包，scala使用下划线
    // 数据的强制转换: java中数据的强制转换使用数据类型+小括号的形式；scala中使用给定的API进行转换:asInstanceOf

    // 不可变数组
    val arr1: Array[Int] = Array(1, 3, 4, 5, 6)
    // 构建一个5个元素大小的数组，数组的数据类型为Int；并且填充默认值
    val arr2: Array[Int] = new Array[Int](5)

    // 数据更新和获取
    // 均基于索引进行操作
    println(s"第1个元素:${arr1(0)}")
    arr1(0) = 1000
    println(s"更新后的第1个元素:${arr1(0)}")

    //  遍历
    for (item <- arr1) {
      println(item)
    }

    // TODO: 对于数组的掌握，这一点就足够了；另外需要的东西其实是数组集合的API的理解以及应用 ==> 所有集合的API均一样

    // 变长数组
    import scala.collection.mutable.ArrayBuffer
    val buf1 = ArrayBuffer[Int]()

    // 元素添加 ==> 尾部追加的形式
    buf1 += 1
    buf1 += 23
    // 将Tuple中的每一个元素作为一个整体加入到Buffer中
    buf1 += (122, 11, 12, 123, 23, 23, 34, 54, 65, 76, 867, 67)
    buf1 ++= Array(12, 12)

    // 指定位置的元素添加
    buf1.insert(0, 1234556)

    // 删除 ==> 只有有数据的情况下才删除，没有对应数据就不做任何操作；并且删除操作每次只会删除一个元素
    buf1 -= 23
    buf1 -= 12
    buf1 -= (11, 122, 1)
    buf1 --= Array(23, 23, 23, 24, 25, 26)

    // 指定位置进行删除
    println(s"被删除的第0位的元素为:${buf1.remove(0)}")

    // 数据更新和获取
    // 均基于索引进行操作
    println(s"第1个元素:${buf1(0)}")
    buf1(0) = 1000
    println(s"更新后的第1个元素:${buf1(0)}")

    //  遍历
    for (item <- buf1) {
      println(item)
    }

    // 一般情况下，优先选择使用Array，除非业务需要否则不使用ArrayBuffer
    // 当可变数组构建完成后，尽量将ArrayBuffer转换为Array进行处理
    val arr3: Array[Int] = buf1.toArray
    val buf2: scala.collection.mutable.Buffer[Int] = arr3.toBuffer

    // 当数组中存储的数据类型不一致的情况下，最终形成的数组，数组元素类型为：数组中所有数据类型的父类
    val arr5: Array[Any] = Array("gerry", 18, "131xxxxxx")
    val name: String = arr5(0).asInstanceOf[String]
    val age: Int = arr5(1).asInstanceOf[Int]
    val phone: String = arr5(2).asInstanceOf[String]
    println(s"name=${name}, age=${age}, phone=${phone}")

  }
}
