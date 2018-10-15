package com.frank.analyzer.streaming

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object StreamingWordCountWithTransformAndForeachRDD {
  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local[10]") // 10启动的spark应用可以并行运行10个task任务
      .setAppName("StreamingWordCountWithTransformAndForeachRDD-wordcount")
    val sc = SparkContext.getOrCreate(conf)
    val ssc = new StreamingContext(sc, Seconds(5))

    // 二、DStream的构建
    val dstream: DStream[String] = ssc.socketTextStream("hive1", 9999)

    // 三、DStream的操作,转换成rdd
    val result: DStream[(String, Int)] = dstream.transform((rdd, time) => {
      // time其实就是批次时间
      rdd
        .flatMap(line => line.split(" "))
        .filter(word => word.nonEmpty)
        .map(word => (word, 1))
        .reduceByKey(_ + _)
        .sortBy(t => t._2, ascending = false)
    })


    // 四、DStream结果数据输出
    // 4.1 结果返回driver输出
    result.print()
    result.foreachRDD((rdd, time) => {
      // time其实是批次时间
      rdd.foreachPartition(iter => {
        // TODO: 这里定义数据输出
        var count = 0
        iter.foreach(r => {
          count += 1
          if (count < 10) {
            println(r)
          }
        })
      })
    })

    // 六、开始运行
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }


}
