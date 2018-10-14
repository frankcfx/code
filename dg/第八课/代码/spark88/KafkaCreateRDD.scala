package spark88

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.streaming.kafka.{Broker, KafkaUtils, OffsetRange}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 上午1:28.
  * 创建topic：
  * kafka-topics.sh --zookeeper localhost:12181/kafka0.9 --create --topic spark11 --partitions 2 --replication-factor 1
  * kafka-topics.sh --zookeeper localhost:12181/kafka0.9 --describe --topic spark11
  * kafka-console-producer.sh --broker-list localhost:9092 --topic spark11
  * kafka-console-consumer.sh --zookeeper localhost:12181/kafka0.9 --topic spark11
  */
object KafkaCreateRDD {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("test").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)

    val fromOffsets = collection.mutable.Map[TopicAndPartition,Tuple2[Long, Long]]()


    val offs = Map("spark11" -> Map(0->Map("from"->12l, "until"->16l),
      1->Map("from"->13l, "until"->17l)),

      "spark22" -> Map(0->Map("from"->13l, "until"->16l),
      1->Map("from"->10l, "until"->18l)))

   val offsetRanges = getOffets(offs)
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> "spark123:9092",
      "auto.offset.reset" -> "smallest"
    )

  // 1. 离线API,返回：RDD of (Kafka message key, Kafka message value)
    //  return RDD of (Kafka message key, Kafka message value)
    /*val rdd = KafkaUtils.createRDD[String, String, StringDecoder, StringDecoder](
      sc, kafkaParams, offsetRanges)
    println("rdd.count()====================》"+rdd.count())
    rdd.collect().foreach(println)
    rdd.map(x=>x._2).collect().foreach(println)*/


   // 2. 离线API,返回自定义类型RDD：
    val messageHandler = (mmd : MessageAndMetadata[String, String]) =>
      (mmd.topic, mmd.partition, mmd.offset, mmd.message())
    val map:Map[TopicAndPartition, Broker] = Map()

    val rdd = KafkaUtils.createRDD[String, String, StringDecoder, StringDecoder,
      (String, Int, Long, String)](sc, kafkaParams, offsetRanges, map, messageHandler)
   println("rdd.count()====================》"+rdd.count())
    rdd.collect().foreach(println)
   rdd.map(x=>x._4).collect().foreach(println)

    //println(offs)
  }

  /**
    * 根据topic和每个topic分区的fromoffset/untiloffests返回OffsetRange数组
    * @param offsetsMap
    * @return
    */
  def getOffets(offsetsMap:Map[String, Map[Int, Map[String, Long]]]) :Array[OffsetRange] = {
    offsetsMap.keys.flatMap(topic=>{
      val topicParts = offsetsMap(topic)
      topicParts.keys.map(partid=>{
        val part = topicParts(partid)
        val from = part("from")
        val until = part("until")
        //println(topic + ":" + partid + ":" + from + ":" + until )
        val tp = TopicAndPartition(topic,partid)
        OffsetRange(tp, from, until)
      })
    }).toArray

  }

}
