package com.ibeifeng.bigdata.spark.app.core

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ibf on 11/19.
  */
object BroadcastVariablesMapJoinSparkCore {
  def main(args: Array[String]): Unit = {
    // 一、创建上下文
    val conf = new SparkConf()
      .setMaster("local") // 如果本地运行，必须指定应用的运行环境， NOTE: 当需要进行集群运行的时候，将该代码注释
      .setAppName("group-sort") // 给定应用名称
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://hadoop-senior01.ibeifeng.com:8020/spark/history") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)

    // 二、rdd创建
    val rdd1 = sc.parallelize(Array(
      ("gerry", 78),
      ("tom", 88),
      ("lili", 97)
    ))
    val rdd2 = sc.parallelize(Array(
      ("gerry", 88),
      ("tom", 77),
      ("lili", 56)
    ))

    // reduce join
    rdd1
      .join(rdd2)
      .map(t => {
        val key = t._1
        val leftValue = t._2._1
        val rightValue = t._2._2
        (key, leftValue, rightValue)
      })
      .foreachPartition(iter => iter.foreach(println))

    // map join
    // 1. 获取rdd的数据
    val rdd2Value = rdd2.collect().toMap
    // 2. 广播变量
    val broadcastValue = sc.broadcast(rdd2Value)
    // 3. 进行数据操作
    rdd1
      .filter(t => broadcastValue.value.contains(t._1))
      .map(t => {
        val key = t._1
        val leftValue = t._2
        val rightValue = broadcastValue.value.getOrElse(t._1, new RuntimeException)
        (key, leftValue, rightValue)
      })
      .foreachPartition(_.foreach(println))
  }
}
