package com.ibeifeng.bigdata.spark.app.streaming

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ibf on 11/26.
  */
object KafkaDirectWordCount2 {
  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local[10]") // 10启动的spark应用可以并行运行10个task任务
      .setAppName("KafkaDirectWordCount2-wordcount")
    val sc = SparkContext.getOrCreate(conf)
    val ssc = new StreamingContext(sc, Seconds(10))

    // 二、DStream的构建
    // kafka连接的consumer参数，只支持两个参数: metadata.broker.list和auto.offset.reset
    val kafkaParams = Map(
      "metadata.broker.list" -> "hadoop-senior01.ibeifeng.com:9092,hadoop-senior01.ibeifeng.com:9093,hadoop-senior01.ibeifeng.com:9094,hadoop-senior01.ibeifeng.com:9095",
      "auto.offset.reset" -> "smallest" // 给定第一次连接的时候，默认的consumer的offset是啥
    )
    // 指定从那个topic的那个分区的那个偏移量开始读取数据
    val fromOffsets = Map(
      TopicAndPartition("beifeng0", 0) -> 100L, // 如果给定的offset偏移量值在kafka对应分区中不存在，那么会报错的
      TopicAndPartition("beifeng0", 1) -> 1000L
    )
    // 给定一个数据处理的函数
    val messageHandler = {
      (mam: MessageAndMetadata[String, String]) => {
        mam.message()
      }
    }
    val dstream: DStream[String] = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, String](
      ssc, // 上下文
      kafkaParams, // kafka的连接参数，只支持一个参数: metadata.broker.list
      fromOffsets, // 指定从那个topic的那个分区的那个偏移量开始读取数据
      messageHandler // 给定一个数据处理的函数
    )

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
