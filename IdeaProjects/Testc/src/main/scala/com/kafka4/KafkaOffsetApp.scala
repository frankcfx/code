package com.kafka4

import com.kafka4.utils.MyKafkaUtils._
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.HasOffsetRanges

/**
  * 验证kafka的offset越界
  */
object KafkaOffsetApp {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("test").setMaster("local[2]")

    val processingInterval = 2

    val brokers = "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095"
    val topic = "mykafka2"
    // Create direct kafka stream with brokers and topics
    val topicsSet = topic.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers,  "auto.offset.reset" -> "smallest")


    /*

    CreateRDD  offsetRanges
    createDirectxxx    fromOffsets

     */

    val ssc = new StreamingContext(sparkConf, Seconds(processingInterval))


    val groupName =  "testp2"
    val messages = createMyDirectKafkaStream(ssc, kafkaParams, topicsSet, groupName)



    messages.foreachRDD((rdd,btime) => {
      if(!rdd.isEmpty()){
        rdd.map(x=>x._2).foreach(println)
        println("==========================:" + rdd.count() )
        println("==========================btime:" + btime )
      }
      saveOffsets(rdd.asInstanceOf[HasOffsetRanges].offsetRanges, groupName)
    })

    ssc.start()
    ssc.awaitTermination()
  }

}
