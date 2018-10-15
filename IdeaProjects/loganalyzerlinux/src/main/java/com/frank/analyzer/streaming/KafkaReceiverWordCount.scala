package com.frank.analyzer.streaming

import kafka.serializer.StringDecoder
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka.KafkaUtils

object KafkaReceiverWordCount {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[5]")
      .setAppName("KafkaReceiverWordCount")
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
      .set("spark.streaming.stopGracefullyOnShutdown", "true") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)

    val ssc = new StreamingContext(sc, Seconds(10))


//    val kafkaStream = KafkaUtils.createStream(streamingContext,
//      [ZK quorum], [consumer group id], [per-topic number of Kafka partitions to consume])

    val kafkaParams: Map[String, String] = Map(
      "group.id" -> "receiver01",
      "zookeeper.connect" -> "zookeeper1:2181/kafka07",
      "auto.offset.reset" -> "smallest"
    )
    val topics: Map[String, Int] = Map("beifeng0" -> 1)

    val kafkaStream: DStream[String] = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc,
      kafkaParams,
      topics,
      StorageLevel.MEMORY_AND_DISK_2
    ).map(_._2)

    /**
      * def createStream[K: ClassTag, V: ClassTag, U <: Decoder[_]: ClassTag, T <: Decoder[_]: ClassTag](
      ssc: StreamingContext,
      kafkaParams: Map[String, String],
      topics: Map[String, Int],
      storageLevel: StorageLevel
    ): ReceiverInputDStream[(K, V)] = {
    val walEnabled = WriteAheadLogUtils.enableReceiverLog(ssc.conf)
    new KafkaInputDStream[K, V, U, T](ssc, kafkaParams, topics, walEnabled, storageLevel)
  }
      */


    val result: DStream[(String, Int)] = kafkaStream.flatMap(line => line.split(" "))
      .map(word => word.toLowerCase())
      .filter(word => word.nonEmpty)
      .map(word => (word,1))
      .reduceByKey((a,b) => a + b)

    result.print()

    ssc.start()
    ssc.awaitTermination()

  }

}
