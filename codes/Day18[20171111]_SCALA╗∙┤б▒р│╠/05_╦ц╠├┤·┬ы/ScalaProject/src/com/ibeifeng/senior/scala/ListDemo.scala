package com.ibeifeng.senior.scala

import java.util

import scala.collection.mutable

/**
  * Created by ibf on 11/11.
  */
object ListDemo {
  def main(args: Array[String]): Unit = {
    // 不可变List
    // List和Array基本类似，区别在于：List不支持更新操作
    // List的数据访问也是基于索引的，从0开始，也是小括号+索引的方式获取数据
    val list11 = List(1, 3, 5, 6, 8, 9)
    // Nil: 表示一个空的List集合
    // 表示将原始添加到空列表中 ===> 等价于:Nil.::(6).::(5).::(4).::(3).::(2).::(1)
    val list2 = 1 :: 2 :: 3 :: 5 :: 6 :: Nil
    val list3 = list11 ::: list2

    // 数据的获取
    println(s"第一个原始的值:${list11(0)}")
    println(s"获取第一个元素的值:${list11.head}")
    println(s"获取除了第一个元素以外的值:${list11.tail}")
    println(s"获取最后一个元素:${list11.last}")
    println(s"获取除了最后一个元素之外的值:${list11.init}")

    // 数据更新 ===> 不支持更新操作
    //    list(0) = 100

    // 循环
    for (item <- list11) {
      print(s"${item}\t")
    }
    println()

    // 不可变List(类似ArrayBuffer)
    import scala.collection.mutable.ListBuffer
    val buf1 = ListBuffer[Int]()

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
    println(buf1.head)
    println(buf1.tail)

    //  遍历
    for (item <- buf1) {
      println(item)
    }

    // 一般情况下，均需要将构建好的buffer转换为其它不可变数据类型
    /*buf1.toArray
    buf1.toList
    buf1.toSet
    buf1.toIndexedSeq
    buf1.toSeq
    buf1.toMap // 转换成为Map，要求集合中的数据类型必须是二元组*/

    // NOTE: 如果想用Java中的数据集合，那么可以直接使用
    val jlist = new util.ArrayList[String]()
    jlist.add("abc")
    jlist.add("def")
    import scala.collection.JavaConverters._
    val slist: mutable.Buffer[String] = jlist.asScala // 将java的集合转换为scala的集合
    slist += "gerry" // 底层实际上是add方法的执行
    val jlist2 = slist.asJava
    jlist2.add("xiaoming")


    println("API说明========================")
    // TODO: API调用过程中使用下划线代替参数的自己整理
    val list: List[Int] = 0.to(20, 2).toList
    // mkString: 将集合转换为一个字符串，根据给定的前缀、分隔符、后缀，这三个参数可选
    println(list.mkString("[", ",", "]"))
    // foreach: 对集合中的每个元素进行迭代处理，使用传入的给定处理函数来进行处理操作, 一般用于数据的输出
    // NOTE: foreach API没有返回值，所以一般不使用foreach对数据进行处理(eg: 数据的转换操作)
    def println2(x: Any) = println(x)

    list.foreach((v: Int) => println2(v))
    list.foreach(v => println2(v))
    list.foreach(v => {
      print(v)
      print("\t")
    })
    println()
    // map: 对集合中的每个元素进行处理，使用给定函数进行数据处理 ===> 数据转换API
    // map API调用的最终结果是返回一个新的集合，新集合中的元素就是给定函数func的返回值
    // NOTE: 给定的函数参数返回的数据类型是啥，最终形成的新的集合中的元素数据类型就是啥
    val map1 = list.map(v => v * 2.0)
    val map2: List[List[Int]] = list.map(v => (0 to v).toList)
    // flatMap和map API一样都是常用的一个数据转换API
    // flatMap: 对集合中的每个元素进行处理，使用给定的函数，并返回一个新的集合，要求给定的函数(参数)的返回值必须是集合
    // NOTE: flatMap其实是在map的基础上做了一次扁平化操作，也就是说，不是把函数的返回值直接作为新集合的元素，而是把返回值(集合)中的元素作为新集合的元素
    // NOTE：最终形成的集合中元素的数据类型其实是给定函数参数返回集合中的数据类型
    // NOTE: flatMap只会做一次扁平化操作
    val flatMap1: List[Int] = list.flatMap(v => (0 to v).toList)
    val flatMap2: List[Array[Int]] = list.flatMap(v => {
      val r: List[Array[Int]] = (0 to v).toList.map(i => (0 to i).toArray)
      r
    })

    // filter: 数据过滤，对集合中的每个元素调用给定函数，如果函数执行返回true，那么该元素保留；否则该元素删除/丢弃
    // filter API最终返回一个新集合，集合中的元素就是那些调用给定函数后，返回值为true的元素
    // filterNot API是filter的求反，true的数据过滤，false的数据保留
    list.filter(v => v % 4 == 0)
    list.filterNot(v => v % 4 == 0)

    // reduce: 数据聚合，根据给定的函数对集合中的元素进行数据聚合操作
    // 先聚合1和2的值，得到一个临时聚合值；然后临时聚合值和3的值聚合，再得到一个临时的聚合值；以此迭代聚合4、5、6、7....的值
    val r1: Int = list.reduce((a: Int, b: Int) => {
      // 执行过程是从左往右执行
      // 第一次执行的时候，a的值为list集合的head元素；以后的执行过程中，a的值是上一个数据处理之后的临时聚合值(函数的返回值)
      // b是list的tail集合的元素迭代
      println(s"a=${a}, b=${b}")
      a + b
    })

    // fold: 功能类似reduce，也是做一个数据聚合的操作(对集合中的所有元素进行操作)；区别在于：fold函数给定一个初始值
    val f1: Int = list.fold(1000)((a, b) => {
      // 执行过程是从左往右执行
      // 第一次执行的时候，a的值为给定的初始值，这里为1000；以后的执行过程中，a的值是上一个数据处理之后的临时聚合值(函数的返回值)
      // b是list的元素迭代
      println(s"a=${a}, b=${b}")
      a + b
    })
    // foldLeft: 功能和fold函数类似，区别在于：foldLeft函数给定的初始值可以是任意数据类型的
    val f2: String = list.foldLeft("")((a, b) => {
      // 执行过程是从左往右执行
      // 第一次执行的时候，a的值为给定的初始值，这里为0；以后的执行过程中，a的值是上一个数据处理之后的临时聚合值(函数的返回值)
      // b是list的元素迭代
      println(s"a=${a}, b=${b}")
      a + ", " + b
    })
    // foldRight: 功能和foldLeft类似，区别在于：foldRight是从右往左计算的
    val f3: String = list.foldRight("")((a, b) => {
      // 执行过程是从左往右执行
      // 第一次执行的时候，b的值为给定的初始值，这里为0；以后的执行过程中，b的值是上一个数据处理之后的临时聚合值(函数的返回值)
      // a是list的元素迭代
      println(s"a=${a}, b=${b}")
      a + ", " + b
    })

    // sorted: 对集合元素进行排序，排序基于默认的排序器(SCALA中的默认排序器只对基本数据类型、元组、字符串进行了实现)；数据排序默认是升序
    // NOTE：元组的排序，先对第一个元素排序，第一个元素相等的情况下，再对第一个元素进行比较排序.....
    list.sorted
    // sortBy: 类似sorted功能，区别在于：根据给定的函数的返回值进行排序的
    // 首先将集合中的元素，按照给定函数执行得到一个排序key的集合，然后对这些key集合进行排序操作(基于默认的排序器)；key集合排序好后，此时key映射的原始的value集合也就排序好了
    list.sortBy(v => v.toString)

    // groupBy:按照给定的函数f对数据进行分组操作，groupBy返回的结果是一个集合，集合数据类型是Map，
    // Map的key是给定函数f的返回数据类型，value就是原始集合中的数据类型
    list.groupBy(v => v % 4)

    // TODO: 作业，计算wordcount
    // 计算每个单词的出现次数，要求: 1. 不区分大小写；2.要求去掉的所有的特殊字符(eg: .,!' ); 3. 必须使用高阶函数，不允许使用while/do-while/for循环进行处理（NOTE:如果在这个过程中，出现不认识的数据集合，那么直接转换为List, eg: xxx.toList）
    val lines = Array(
      "hadoop, spark, hbase",
      "spark' hbase hiVE",
      "",
      "spark, hive. Spark!",
      " ",
      "spark, spark HBase hive spark"
    )
  }
}
