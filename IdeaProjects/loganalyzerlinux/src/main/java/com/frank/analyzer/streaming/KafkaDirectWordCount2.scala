package com.frank.analyzer.streaming

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}

object KafkaDirectWordCount2 {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[5]")
      .setAppName("KafkaDirectWordCount2")
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
      .set("spark.streaming.stopGracefullyOnShutdown", "true") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)

    val ssc = new StreamingContext(sc, Seconds(10))

    val kafkaParams: Map[String, String] = Map(
      "metadata.broker.list" -> "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095"
    )

    val fromOffsets: Map[TopicAndPartition, Long] = Map(
      TopicAndPartition("beifeng0", 0) -> 100L,
      TopicAndPartition("beifeng0", 1) -> 1000L
    )
//    val messageHandler: MessageAndMetadata[K, V] => R = { }

    val messageHandler =  (mam:MessageAndMetadata[String, String]) => {
        mam.message()
      }

    val directKafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder,String](
      ssc, kafkaParams, fromOffsets, messageHandler)

    val result: DStream[(String, Int)] = directKafkaStream.flatMap(line => line.split(" "))
      .map(word => word.toLowerCase())
      .filter(word => word.nonEmpty)
      .map(word => (word,1))
      //      .reduceByKey((a,b) => a + b)
      .reduceByKey(_+_)

    result.print()

    ssc.start()
    ssc.awaitTermination()

  }

}
