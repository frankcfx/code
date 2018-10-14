package com.ibeifeng.spark.sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 1.6版本的编程方式在2.x版本中运行
  */
object SparkSQLDemo1 {
  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("demo")
    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new SQLContext(sc)
    //        new HiveContext(sc)
    import org.apache.spark.sql.functions._
    import sqlContext.implicits._

    // 二、RDD构建
    val rdd1 = sc.parallelize(Array(
      ("gerry", "c1", 80),
      ("tom", "c1", 77),
      ("lili", "c1", 97),
      ("张三", "c2", 67),
      ("李四", "c2", 66),
      ("王五", "c2", 54),
      ("孙二", "c2", 59)
    ))

    // RDD转换DataFrame
    val df = rdd1.toDF("name", "cla", "source")

    // DataFrame注册临时表
    df.registerTempTable("tmp01")
    sqlContext.sql("select cla, avg(source) as avg1 from tmp01 group by cla").show(false)
    sqlContext.sql("select name, source from tmp01 where source >= 60").show(false)

    // DSL语法
    df
      .where("source >= 60")
      .select("name", "source")
      .show(false)

    df
      .select("cla", "source")
      .groupBy("cla")
      .agg(
        avg("source").as("avg_source"),
        sum("source").as("sum_source")
      )
      .show(false)
  }
}
