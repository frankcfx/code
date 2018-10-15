package com.kafka

import kafka.serializer.StringDecoder
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Duration, Seconds, StreamingContext}


object StreamingWithCheckpoint {
  def main(args: Array[String]) {
    //val Array(brokers, topics) = args
    val processingInterval = 2
    val brokers = "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095"
    val topics = "mykafka1"
    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("ConsumerWithCheckPoint").setMaster("local[2]")
    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers,
      "auto.offset.reset" -> "smallest")

    val checkpointPath = "hdfs://ns1/spark/streaming/spark_checkpoint10"


    def functionToCreateContext(): StreamingContext = {
      val ssc = new StreamingContext(sparkConf, Seconds(processingInterval))
      val messages = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topicsSet)

      ssc.checkpoint(checkpointPath)
      messages.checkpoint(Duration(8*processingInterval.toInt*1000))
      messages.foreachRDD(rdd => {
        if(!rdd.isEmpty()){
          println("################################" + rdd.count())
        }

      })
      ssc
    }

    // 如果有checkpoint则checkpoint中记录的信息恢复StreamingContext
    val context = StreamingContext.getOrCreate(checkpointPath, functionToCreateContext _)
    context.start()
    context.awaitTermination()
  }
}