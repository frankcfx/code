package spark88.utils

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.ZkUtils
import org.apache.hadoop.hbase.client.{ConnectionFactory, Put, Scan}
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by hadoop on 下午4:52.
  */
object KafkaHbaseManager {

  def saveOffsets(TOPIC_NAME:String,GROUP_ID:String,offsetRanges:Array[OffsetRange],
                  hbaseTableName:String,batchTime: String) ={
    val hbaseConf = HBaseConfiguration.create()
    hbaseConf.addResource("/etc/hbase/conf/hbase-site.xml")
    val conn = ConnectionFactory.createConnection(hbaseConf)
    val table = conn.getTable(TableName.valueOf(hbaseTableName))
    val rowKey = TOPIC_NAME + ":" + GROUP_ID + ":" + batchTime
    val put = new Put(rowKey.getBytes)
    for(offset <- offsetRanges){
      put.addColumn(Bytes.toBytes("offsets"),Bytes.toBytes(offset.partition.toString),
        Bytes.toBytes(offset.untilOffset.toString))
    }
    table.put(put)  // 行级事务
    conn.close()
  }


  def getNumberOfPartitionsForTopicFromZK(TOPIC_NAME:String,GROUP_ID:String,
                                          zkQuorum:String,zkRootDir:String,sessTimeout:Int,connTimeOut:Int): Int ={
    val zkUrl = zkQuorum+"/"+zkRootDir
    val zkClientAndConn= ZkUtils.createZkClientAndConnection(zkUrl, sessTimeout,connTimeOut)
    val zkUtils = new ZkUtils(zkClientAndConn._1, zkClientAndConn._2,false)
    val zKPartitions= zkUtils.getPartitionsForTopics(Seq(TOPIC_NAME
    )).get(TOPIC_NAME).toList.head.size
    println(zKPartitions)
    zkClientAndConn._1.close()
    zkClientAndConn._2.close()
    zKPartitions
  }


  def getFromOffsetResults(TOPIC_NAME:String,GROUP_ID:String,hTableName:String,
                              zkQuorum:String,zkRootDir:String,sessTimeout:Int,connTimeOut:Int):Tuple2[String,Map[TopicAndPartition,Long]] ={


    val zKNumberOfPartitions =getNumberOfPartitionsForTopicFromZK(TOPIC_NAME, GROUP_ID, zkQuorum,zkRootDir,sessTimeout,connTimeOut)

    val hbaseConf = HBaseConfiguration.create()

    // 获取hbase中最后提交的offset
    val conn = ConnectionFactory.createConnection(hbaseConf)
    val table = conn.getTable(TableName.valueOf(hTableName))
    val startRow = TOPIC_NAME + ":" + GROUP_ID + ":" + String.valueOf(System.currentTimeMillis())
    val stopRow = TOPIC_NAME + ":" + GROUP_ID + ":" + 0
    val scan = new Scan()
    val scanner = table.getScanner(scan.setStartRow(startRow.getBytes).setStopRow(stopRow.getBytes)
      .setReversed(true))
    val result = scanner.next()
    var hbaseNumberOfPartitions = 0 // 在hbase中获取的分区数量
    var BATCH_ID = TOPIC_NAME + "-" + GROUP_ID + "-1"
    if (result != null){
      // 将分区数量设置为hbase表的列数量
      hbaseNumberOfPartitions = result.listCells().size()
      val rowkey = new String(result.getRow)
      BATCH_ID = rowkey.replaceAll(":", "-")
    }

    val fromOffsets = collection.mutable.Map[TopicAndPartition,Long]()
    if(hbaseNumberOfPartitions == 0){
      // 初始化kafka为开始
      for (partition <- 0 to zKNumberOfPartitions-1){
        fromOffsets += ((TopicAndPartition(TOPIC_NAME,partition), 0))
      }
    } else if(zKNumberOfPartitions > hbaseNumberOfPartitions){
      // 处理新增加的分区添加到kafka的topic
      for (partition <- 0 to hbaseNumberOfPartitions-1){
        val fromOffset = Bytes.toString(result.getValue(Bytes.toBytes("offsets"),
          Bytes.toBytes(partition.toString)))
        fromOffsets += ((TopicAndPartition(TOPIC_NAME,partition), fromOffset.toLong))
      }
      for (partition <- hbaseNumberOfPartitions to zKNumberOfPartitions-1){
        fromOffsets += ((TopicAndPartition(TOPIC_NAME,partition), 0))
      }
    } else {
      // 获取上次运行的offset
      for (partition <- 0 to hbaseNumberOfPartitions-1 ){
        val fromOffset = Bytes.toString(result.getValue(Bytes.toBytes("offsets"),
          Bytes.toBytes(partition.toString)))
        fromOffsets += ((TopicAndPartition(TOPIC_NAME,partition), fromOffset.toLong))
      }
    }

    scanner.close()
    conn.close()
    (BATCH_ID, fromOffsets.toMap)
  }

  def main(args: Array[String]): Unit = {
   // getLastCommittedOffsets("mytest1", "testp", "stream_kafka_offsets", "spark123:12181", "kafka0.9", 30000, 30000)

    val processingInterval = 2
    val brokers = "spark123:9092"
    val topics = "mytest1"
    // Create context with 2 second batch interval
    val sparkConf = new SparkConf().setAppName("kafkaHbase").setMaster("local[2]")
    // Create direct kafka stream with brokers and topics
    val topicsSet = topics.split(",").toSet
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers,
      "auto.offset.reset" -> "smallest")


    val ssc = new StreamingContext(sparkConf, Seconds(processingInterval))
    val groupId = "testp"
    val hbaseTableName = "spark_kafka_offsets"

    // 获取kafkaStream
    //val kafkaStream = createMyDirectKafkaStream(ssc, kafkaParams, zkClient, topicsSet, "testp")
    val messageHandler = (mmd : MessageAndMetadata[String, String]) => (mmd.topic, mmd.message())
     val fromOffsetResults = getFromOffsetResults("mytest1", groupId,hbaseTableName , "spark123:12181", "kafka0.9", 30000, 30000)
    val batchid = fromOffsetResults._1
    val fromOffsets = fromOffsetResults._2

    var kafkaStream : InputDStream[(String, String)] = null
    kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParams, fromOffsets, messageHandler)


    kafkaStream.foreachRDD((rdd,btime) => {
      if(!rdd.isEmpty()){
        println("==========================:" + rdd.count() )
        println("==========================btime:" + btime.toString() )
        saveOffsets(topics, groupId, rdd.asInstanceOf[HasOffsetRanges].offsetRanges, hbaseTableName, btime.toString())
      }
    })

    //val offsetsRanges:Array[OffsetRange] = null

    ssc.start()
    ssc.awaitTermination()


  }
}
