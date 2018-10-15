package com.kafka4

import com.kafka4.utils.MyKafkaUtils._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.HasOffsetRanges
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * 限速参数：spark.streaming.kafka.maxRatePerPartition  每秒每个分区的记录数
  */
object KafkaRate {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().set("spark.streaming.kafka.maxRatePerPartition", "100")
      .setAppName("test").setMaster("local[2]")

    val processingInterval = 4
    val brokers = "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095"
    val topic = "mykafka2"
    // Create direct kafka stream with brokers and topics
    val topicsSet = topic.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers,  "auto.offset.reset" -> "smallest")

    val ssc = new StreamingContext(sparkConf, Seconds(processingInterval))

    val groupName =  "testp2"
    val messages = createMyDirectKafkaStream(ssc, kafkaParams, topicsSet, groupName)

    messages.foreachRDD((rdd,btime) => {
      if(!rdd.isEmpty()){
        println("==========================:" + rdd.count() )
        println("==========================btime:" + btime )
      }
      saveOffsets(rdd.asInstanceOf[HasOffsetRanges].offsetRanges, groupName)
    })

    ssc.start()
    ssc.awaitTermination()
  }
}