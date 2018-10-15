package com.kafka

import kafka.serializer.StringDecoder
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object HAKafkaDirectWordCount {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[5]")
      .setAppName("HAKafkaDirectWordCount")
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
      .set("spark.streaming.stopGracefullyOnShutdown", "true") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)

    // checkpoint文件夹路径一定要求是hdfs上的一个路径，方便后续的一个恢复
    val checkpointDirPath = "hdfs://ns1/spark/streaming/ha01"

    def createStreamingContextFunc(): StreamingContext = {
      val ssc = new StreamingContext(sc, Seconds(10))

      // 二、DStream的构建
      // kafka连接的consumer参数，只支持两个参数: metadata.broker.list和auto.offset.reset
      val kafkaParams = Map(
        "metadata.broker.list" -> "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095",
        "auto.offset.reset" -> "smallest" // 给定第一次连接的时候，默认的consumer的offset是啥
      )
      // 给定消费那些topic的数据，topic名称构成的一个集合
      val topics = Set("beifeng0")
      val dstream: DStream[String] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
        ssc: StreamingContext, // 上下文
        kafkaParams, // Kafka的consumer的配置参数
        topics // 给定消费那些topic的数据，topic名称构成的一个集合
      ).map(_._2)

      // 三、DStream的操作
      val result: DStream[(String, Int)] = dstream
        .flatMap(line => line.split(" "))
        .map(word => {
          (word, 2)
        })
        .filter(t => t._1.nonEmpty)
        .reduceByKey((a: Int, b: Int) => a + b)

      // 一般HA需要结合mapPartitions API一起使用, 更改mapPartitions 不会导致内部的DAG图变化
      //要求：宕机之前和恢复之后的SparkStreaming程序的执行DAG图完全一样
      //没有suffle操作的都可以在mapPartitions执行
      dstream.mapPartitions(iter => {
        //没有suffle操作的都可以在mapPartitions执行
        iter
          .filter(line => line.nonEmpty)
          .flatMap(line => line.split(" "))
          .map(word => {
            (word, 3)
          })
          .filter(t => t._1.nonEmpty)
      }).reduceByKey(_ + _).print()

      // 四、DStream结果数据输出
      // 4.1 结果返回driver输出
      result.print()

      // 设置checkpoint文件夹路径地址
      ssc.checkpoint(checkpointDirPath)

      // 返回StreamingContext
      ssc
    }

    // 构建StreamingContext对象
    val ssc = StreamingContext.getActiveOrCreate(
      checkpointPath = checkpointDirPath, // 指定hdfs上的checkpoint文件夹路径
      creatingFunc = createStreamingContextFunc // 给定当文件夹不存在或者文件夹为空的时候，新建StreamingContext对象的方法
    )

    // NOTE：在这里不允许进行任何DStream的操作


    // 六、开始运行
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }

}
