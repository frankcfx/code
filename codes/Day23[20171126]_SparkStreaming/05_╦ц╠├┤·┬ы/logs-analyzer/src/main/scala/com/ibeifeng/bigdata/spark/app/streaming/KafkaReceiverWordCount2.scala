package com.ibeifeng.bigdata.spark.app.streaming

import kafka.serializer.StringDecoder
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ibf on 11/26.
  */
object KafkaReceiverWordCount2 {
  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local[10]") // 10启动的spark应用可以并行运行10个task任务
      .setAppName("KafkaReceiverWordCount2-wordcount")
      .set("spark.streaming.blockInterval", "5s")
    val sc = SparkContext.getOrCreate(conf)
    val ssc = new StreamingContext(sc, Seconds(10))

    // 二、DStream的构建
    // Kafka的consumer的配置参数
    val kafkaParams = Map(
      "zookeeper.connect" -> "hadoop-senior01.ibeifeng.com:2181/o2o17",
      "group.id" -> "receiver02", // 给定consuemr group id
      "auto.offset.reset" -> "largest" // 给定第一次连接的时候，默认的consumer的offset是啥
    )
    // 指定读取那个topic的数据，以及该topic使用几个KafkaStream流进行数据读取
    val topics = Map("beifeng0" -> 1)
    val dstream0: DStream[String] = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc: StreamingContext, // 上下文
      kafkaParams, // Kafka的consumer的配置参数
      topics, // 指定读取那个topic的数据，以及该topic使用几个KafkaStream流进行数据读取
      StorageLevel.MEMORY_AND_DISK_2 // 指定Block块的缓存级别
    ).map(_._2)
    val dstream1: DStream[String] = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc: StreamingContext, // 上下文
      kafkaParams, // Kafka的consumer的配置参数
      topics, // 指定读取那个topic的数据，以及该topic使用几个KafkaStream流进行数据读取
      StorageLevel.MEMORY_AND_DISK_2 // 指定Block块的缓存级别
    ).map(_._2)
    val dstream: DStream[String] = dstream0.union(dstream1)

    // 三、DStream的操作
    val result: DStream[(String, Int)] = dstream
      .flatMap(line => line.split(" "))
      .map(word => {
        (word, 1)
      })
      .filter(t => t._1.nonEmpty)
      .reduceByKey((a: Int, b: Int) => a + b)

    // 四、DStream结果数据输出
    // 4.1 结果返回driver输出
    result.print()

    // 六、开始运行
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }
}
