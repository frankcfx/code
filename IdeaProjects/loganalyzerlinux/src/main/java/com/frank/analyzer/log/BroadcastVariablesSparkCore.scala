package com.frank.analyzer.log

import org.apache.spark.{SparkConf, SparkContext}

object BroadcastVariablesSparkCore {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("BroadcastVariables")
      //设置Master_IP
      .setMaster("spark://spark-master1:7070")
      //提交的jar包在你本机上的位置
      .setJars(List("/home/cfx/IdeaProjects/loganalyzerlinux/target/log-analyzer-1.0-SNAPSHOT.jar"))
      //设置driver端的ip,这里是你本机的ip
      .setIfMissing("spark.eventLog.enabled", "true")
      .setIfMissing("spark.executor.memory", "2g")
      .setIfMissing("spark.eventLog.dir", "hdfs://ns1/spark/history")

    //init context
    //    val sc = new SparkContext(sparkConf)
    val sc = SparkContext.getOrCreate(sparkConf)

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
