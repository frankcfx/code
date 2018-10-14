package com.ibeifeng.spark.sql

import org.apache.spark.sql._

/**
  * 基于sql编程
  * 基本和spark1.6类似，主要区别：
  * 1. 入口变了
  * 2. 临时表变成了当前会话的临时的视图和全局的临时视图
  */
object SparkSQLDemo2 {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建 ==> SparkSession构建
    val spark = SparkSession
      .builder()
      .appName("demo")
      .master("local")
      // .enableHiveSupport() // 集成hive，集成hive需要做的操作和1.6完全一样(需要添加pom.xml中的内容以及hive-site.xml文件等相关配置信息)
      .getOrCreate()

    // 二、读取json格式的数据形成Dataset
    val ds: Dataset[Row] = spark.read.json("data/people.json")
    ds.show()

    // Dataset注册临时表 ==> 2.x版本中，临时表更改为临时的视图 ==> 当前会话视图
    ds.createOrReplaceTempView("people")
    spark.sql("select * from people").show()

    // Dataset注册全局的视图 ==> 在程序结束前，创建的视图均有效(在不同的会话中均可以访问)
    ds.where("age is not null").createOrReplaceGlobalTempView("people")
    // 全局视图的使用必须加前缀:"global_temp."
    spark.sql("select * from global_temp.people").show()
    // 在一个新的会话中照样可以访问到全局视图
    spark.newSession().sql("select * from global_temp.people").show()

    // 查看表和Database
    spark.sql("show databases").show()
    spark.sql("show tables").show()
    // 其实全局视图在show tables中不会显示的
    spark.newSession().sql("show tables").show()

    // 结果输出
    spark
      .sql("select * from global_temp.people")
      .write
      .format("csv")
      .mode(SaveMode.Overwrite)
      .save("result/csv")

    val rdd = spark.read.table("global_temp.people").rdd
    rdd.foreachPartition(iter => iter.foreach(println))

  }
}
