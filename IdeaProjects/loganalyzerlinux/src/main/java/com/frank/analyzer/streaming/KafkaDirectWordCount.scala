package com.frank.analyzer.streaming

import kafka.serializer.StringDecoder
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}

object KafkaDirectWordCount {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[5]")
      .setAppName("KafkaDirectWordCount")
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
      .set("spark.streaming.stopGracefullyOnShutdown", "true") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)

    val ssc = new StreamingContext(sc, Seconds(10))

    val kafkaParams: Map[String, String] = Map(
//      "group.id" -> "receiver01",
      "metadata.broker.list" -> "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095",
      "auto.offset.reset" -> "smallest"
    )
    val topics: Set[String] = Set("beifeng0")

    val directKafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](
      ssc, kafkaParams, topics).map(_._2)

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
