package com.frank.analyzer.streaming

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object WindowWordCount2 {

  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local[10]") // 10启动的spark应用可以并行运行10个task任务
      .setAppName("WindowWordCount2-wordcount")
    val sc = SparkContext.getOrCreate(conf)
    val ssc = new StreamingContext(sc, Seconds(1))
    ssc.checkpoint(s"hdfs://ns1/spark/streaming/ha-${System.currentTimeMillis()}")


    // 二、DStream的构建
    val dstream: DStream[String] = ssc.socketTextStream("hive1", 9999)

    // 三、DStream的操作
    val result = dstream
      .flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .filter(t => t._1.nonEmpty)
      .reduceByKeyAndWindow(
        (a: Int, b: Int) => a + b, // 给定相同key的聚合函数，和reduceByKey API的参数一样
        (c: Int, d: Int) => c - d, // c表示的是上一个批次的执行结果，d表示的是上一个批次和当前批次没有重叠部分的结果值
        Seconds(10), // windowDuration: Duration ==> 指定窗口大小，表示每个批次计算的数据是最近多久的数据，该参数要求是父DStream的批次间隔时间的整数倍(>0)
        Seconds(5), //slideDuration: Duration ==> 指定滑动大小，表示形成的DStream中间隔多久产生一个批次，该参数要求是父DStream的批次间隔的整数倍(>0)
        filterFunc = t => t._2 > 0 // 指定过滤函数，当数据等于0的时候表示没有出现了，那么直接过滤掉
      )


    // 四、DStream结果数据输出
    // 4.1 结果返回driver输出
    result.print()

    // 六、开始运行
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }

}
