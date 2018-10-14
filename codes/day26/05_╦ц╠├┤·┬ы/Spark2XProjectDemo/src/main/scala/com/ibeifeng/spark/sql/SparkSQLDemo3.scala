package com.ibeifeng.spark.sql

import org.apache.spark.sql.{Dataset, SparkSession}

/**
  */
object SparkSQLDemo3 {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建 ==> SparkSession构建
    val spark = SparkSession
      .builder()
      .appName("demo")
      .master("local")
      .getOrCreate()
    import org.apache.spark.sql.functions._
    import spark.implicits._

    // 二、构建一个Dataset
    val ds: Dataset[(String, String, Int)] = spark.createDataset(Seq(
      ("gerry", "c1", 80),
      ("tom", "c1", 77),
      ("lili", "c1", 97),
      ("张三", "c2", 67),
      ("李四", "c2", 66),
      ("王五", "c2", 54),
      ("孙二", "c2", 59)
    ))
    ds.show()

    // Dataset操作 ==> 基本当做RDD进行操作即可，或者当做DataFrame进行DSL开发也可以
    ds
      .where("_3 >= 60")
      .groupBy("_2")
      .agg(
        avg("_3").as("avg1"),
        sum("_3").as("sum1"),
        count("_3").as("count1")
      )
      .show(false)

    ds
      .filter(t => t._3 >= 60)
      .map(t => (t._2, t._3))
      .groupByKey(t => t._1)
      .mapGroups((className, iter) => {
        // iter迭代器只能从头到尾迭代一次，如果代码中迭代多次的话，那么后面的迭代会认为iter流为空
        // 因为只能迭代一次：所以在Dataset编程中，groupByKey这个API的底层实现来件是不存在OOM
        /*// 求sum&count&avg
        val sum = iter.map(t => t._2).sum
        val count = iter.size // 这个是iter的第二次迭代，所以count值为0
        val avg = 1.0 * sum / count*/
        val (sum, count) = iter.foldLeft((0.0, 0))((a, b) => {
          (a._1 + b._2, a._2 + 1)
        })
        val avg = sum / count
        (className, avg, sum, count)
      })
      .show(false)

    /*ds.rdd
    ds.toDF()*/

  }
}
