package com.kafka


import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.ZkConnection
//import kafkautils.KafkaZKManager.{getFromOffsets, storeOffsets}
import org.I0Itec.zkclient.ZkClient
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put, Scan} //hbase0.98.6 不支持ConnectionFactory
//import org.apache.hadoop.hbase.client.{ConnectionFactory, Put, Scan}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.kafka.common.TopicPartition
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}
//import kafka.ZookeeperHelper.client
import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import org.I0Itec.zkclient.ZkClient
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.ExponentialBackoffRetry
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataOutputStream, FileSystem, Path}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}

import scala.collection.JavaConversions._
/**
  * Created by zhoucw on 下午4:52.
  *
  */
object KafkaHbaseManager {

  // 自己参考实现
  def saveOffsets(TOPIC_NAME:String,GROUP_ID:String,offsetRanges:Array[OffsetRange],
                  hbaseTableName:String,batchTime: org.apache.spark.streaming.Time) ={

    val hbaseConf = HBaseConfiguration.create()
    val conn = ConnectionFactory.createConnection(hbaseConf)
    val table = conn.getTable(TableName.valueOf(hbaseTableName))
    val rowkey = TOPIC_NAME + ":" + GROUP_ID + ":" + String.valueOf(batchTime.milliseconds)
    val put = new Put(rowkey.getBytes())
    for(offset <- offsetRanges){
      put.addColumn(Bytes.toBytes("offsets"),Bytes.toBytes(offset.partition.toString),
        Bytes.toBytes(offset.untilOffset.toString))
    }
    table.put(put)
    conn.close()

  }


  // 从zookeeper中获取topic的分区数
  def getNumberOfPartitionsForTopicFromZK(TOPIC_NAME:String,GROUP_ID:String,
                                          zkQuorum:String,zkRootDir:String,sessTimeout:Int,connTimeOut:Int): Int ={
    //zookeeper1:2181,zookeeper2:2181,zookeeper3:2181/kafka07
    val zkUrl = zkQuorum + "/" + zkRootDir
//    val zkClientAndConn: (ZkClient, ZkConnection) = ZkUtils.createZkClientAndConnection(zkUrl,sessTimeout,connTimeOut)
    val zkClientAndConn = ZkUtils.createZkClientAndConnection(zkUrl,sessTimeout,connTimeOut)
    val zkUtils = new ZkUtils(zkClientAndConn._1, zkClientAndConn._2, false)
    val zKPartitions = zkUtils.getPartitionsForTopics(Seq(TOPIC_NAME)).get(TOPIC_NAME).toList.head.size
//    val zKPartitions = zkUtils.getPartitionsForTopics(Seq(TOPIC_NAME)).get(TOPIC_NAME)// return Some(ArrayBuffer(0, 1, 2))
    println(zKPartitions)
    zkClientAndConn._1.close()
    zkClientAndConn._2.close()
    zKPartitions

  }

  // 自己参考实现
  def getLastestOffsets(TOPIC_NAME:String,GROUP_ID:String,hTableName:String,
                        zkQuorum:String,zkRootDir:String,sessTimeout:Int,connTimeOut:Int):Map[TopicAndPartition,Long] ={

    val zKNumberOfPartitions =getNumberOfPartitionsForTopicFromZK(TOPIC_NAME, GROUP_ID, zkQuorum,zkRootDir,sessTimeout,connTimeOut)

    val hbaseConf = HBaseConfiguration.create()

    // 获取hbase中最后提交的offset
    val conn = ConnectionFactory.createConnection(hbaseConf)
    val table = conn.getTable(TableName.valueOf(hTableName))
    val startRow = TOPIC_NAME + ":" + GROUP_ID + ":" + String.valueOf(System.currentTimeMillis())
    val stopRow = TOPIC_NAME + ":" + GROUP_ID + ":" + 0
    val scan = new Scan()
    //HBase数据的行键为时间，扫描时按照时间从前往后排列。使用HBase时需要获取最新插入的一条数据，也就是“最后”一条数据。由于条数很多，
    //不可能从前往后扫描；也不太希望使用“大数减去时间”作为行键的方式强制其最新时间放在最前。有没有更好的解决方式？
    //hbase0.98版本之后的Scan支持setReversed()函数。这个函数可以从后往前地扫描数据。有意思的是，扫描时设置的startRow和stopRow必须正好反过来。
    //Scan.setReversed(true) If you specify a startRow and stopRow, to scan in reverse, the startRow needs to be lexicographically after the stopRow.
    val scanner = table.getScanner(scan.setStartRow(startRow.getBytes).setStopRow(stopRow.getBytes).setReversed(true))
    val result = scanner.next()

    var hbaseNumberOfPartitions = 0 // 在hbase中获取的分区数量
    if (result != null){
      // 将分区数量设置为hbase表的列数量
      hbaseNumberOfPartitions = result.listCells().size()
    }

    val fromOffsets = collection.mutable.Map[TopicAndPartition,Long]()
    if(hbaseNumberOfPartitions == 0){
      // 初始化kafka为开始
      for(partition <- 0 to zKNumberOfPartitions-1) {
        fromOffsets += ((TopicAndPartition(TOPIC_NAME, partition), 0))
      }

    } else if(zKNumberOfPartitions > hbaseNumberOfPartitions){
      // 处理新增加的分区添加到kafka的topic
      for(partition <- 0 to hbaseNumberOfPartitions -1){
        val fromOffset = Bytes.toString(result.getValue(Bytes.toBytes("offsets"),
          Bytes.toBytes(partition.toString)))
        fromOffsets += ((TopicAndPartition(TOPIC_NAME, partition), fromOffset.toLong))
      }

      for(partition <- hbaseNumberOfPartitions to zKNumberOfPartitions -1) {
        fromOffsets += ((TopicAndPartition(TOPIC_NAME,partition), 0))
      }

    } else {
      // 获取上次运行的offset
      for(partition <- 0 to hbaseNumberOfPartitions -1) {
        val fromOffset = Bytes.toString(result.getValue(Bytes.toBytes("offsets"),
          Bytes.toBytes(partition.toString)))
        fromOffsets += ((TopicAndPartition(TOPIC_NAME, partition), fromOffset.toLong))
      }

    }

    scanner.close()
    conn.close()
    fromOffsets.toMap
  }

  def main(args: Array[String]): Unit = {
    // getLastCommittedOffsets("mytest1", "testp", "stream_kafka_offsets", "spark123:12181", "kafka0.9", 30000, 30000)

    val processingInterval = 2
    val brokers = "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095"
    val topics = "mykafka2"
    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("kafkahbase").setMaster("local[2]")
    // Create direct kafka stream with brokers and topics
//    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String](
      "metadata.broker.list" -> brokers,
      "auto.offset.reset" -> "smallest")


    val ssc = new StreamingContext(sparkConf, Seconds(processingInterval))
    val groupId = "testp"
    val hbaseTableName = "spark_kafka_offsets"

    // 获取kafkaStream
    //val kafkaStream = createMyDirectKafkaStream(ssc, kafkaParams, zkClient, topicsSet, "testp")
    val messageHandler = (mmd : MessageAndMetadata[String, String]) => (mmd.topic, mmd.message())

//    def getLastestOffsets(TOPIC_NAME:String,GROUP_ID:String,hTableName:String,zkQuorum:String,zkRootDir:String,sessTimeout:Int,connTimeOut:Int)
    val zkQuorum = "zookeeper1:2181,zookeeper2:2181,zookeeper3:2181"
    val zkRootDir = "kafka07"
//    zookeeper1:2181,zookeeper2:2181,zookeeper3:2181/kafka07
    val fromOffsets = getLastestOffsets(topics, groupId, hbaseTableName, zkQuorum, zkRootDir, 30000, 30000)


    var kafkaStream : InputDStream[(String, String)] = null
    kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParams, fromOffsets, messageHandler)


    kafkaStream.foreachRDD((rdd,btime) => {
      if(!rdd.isEmpty()){
        println("==========================:" + rdd.count() )
        println("==========================btime:" + btime )
        saveOffsets(topics, groupId, rdd.asInstanceOf[HasOffsetRanges].offsetRanges, hbaseTableName, btime)
      }

    })


    //val offsetsRanges:Array[OffsetRange] = null

    ssc.start()
    ssc.awaitTermination()


  }
}
