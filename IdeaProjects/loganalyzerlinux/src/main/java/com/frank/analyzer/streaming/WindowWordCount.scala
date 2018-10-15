package com.frank.analyzer.streaming

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object WindowWordCount {

  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local[5]")
      .setAppName("HAKafkaDirectWordCount")
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
      .set("spark.streaming.stopGracefullyOnShutdown", "true") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)

    val ssc = new StreamingContext(sc, Seconds(5))

    // 二、DStream的构建
    val dstream: DStream[String] = ssc.socketTextStream("hive1", 9999)

    // 三、DStream的操作
    val result = dstream
      .flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .filter(t => t._1.nonEmpty)
      .reduceByKeyAndWindow(
        (a: Int, b: Int) => a + b, // 给定相同key的聚合函数，和reduceByKey API的参数一样
        Seconds(15), // windowDuration: Duration ==> 指定窗口大小，表示每个批次计算的数据是最近多久的数据，该参数要求是父DStream的批次间隔时间的整数倍(>0)
        Seconds(10) //slideDuration: Duration ==> 指定滑动大小，表示形成的DStream中间隔多久产生一个批次，该参数要求是父DStream的批次间隔的整数倍(>0)
      )


    // 四、DStream结果数据输出
    // 4.1 结果返回driver输出
    result.print()

    // 六、开始运行
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }

}
