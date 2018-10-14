package spark88

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka4.utils.MyKafkaUtils.getResetOffsets
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.streaming.kafka.{Broker, KafkaUtils, OffsetRange}
import org.apache.spark.{SparkConf, SparkContext, TaskContext}
import spark88.utils.KafkaHbaseManager.getFromOffsetResults
import spark88.utils.{KafkaHbaseManager, ParseUtils}

import scala.collection.mutable


/**
  * Created by hadoop on 上午1:28.
  * 创建topic：
  * kafka-topics.sh --zookeeper localhost:12181/kafka0.9 --create --topic myat --partitions 2 --replication-factor 1
  * kafka-topics.sh --zookeeper localhost:12181/kafka0.9 --describe --topic myat
  * kafka-console-producer.sh --broker-list localhost:9092 --topic myat
  * kafka-console-consumer.sh --zookeeper localhost:12181/kafka0.9 --topic myat
  */
object KafkaETL {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("test").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)

    val brokers = "spark1234:9092"
    val topic = "myat"
    val topics = topic.split(",").toSet
    val groupName = "testg"
    val zQuorum = "spark123:12181"
    val zkRootDir = "kafka0.9"
    val sessionTimeOut = 3000
    val connTimeOut = 3000
    val hbaseTableName = "spark_kafka_offsets"
    val outputPath = "/hadoop/kafka22/"



    // 获取topic中有效的开始offset
    val kafkaParams = Map[String, String]("metadata.broker.list" -> brokers, "auto.offset.reset" -> "smallest")
    val fromOffsetResults = KafkaHbaseManager.getFromOffsetResults(topic, groupName, hbaseTableName, zQuorum, zkRootDir, sessionTimeOut, connTimeOut)
    val batchid = fromOffsetResults._1
    val fromOffsets = fromOffsetResults._2

    // 获取topic中有效的最新offset
    val kafkaParamsLargest = Map[String, String]("metadata.broker.list" -> brokers, "auto.offset.reset" -> "largest")
    val largestOffsets = getResetOffsets(kafkaParamsLargest, topics)


    // 每个partition消费最大消息条数
    val maxMsgNumPerPartition = 100000l
    val offsetRanges = fromOffsets.keys.map(tp => {
      val fromOffset = fromOffsets(tp)
      val largestOffset = largestOffsets(tp)
      val untilOffset = Math.min(largestOffset, fromOffset + maxMsgNumPerPartition)
      OffsetRange(tp, fromOffset, untilOffset)
    }).toArray


    val messageHandler = (mmd: MessageAndMetadata[String, String]) => {
      (mmd.offset, mmd.topic, mmd.partition, mmd.message())
    }


    val map: Map[TopicAndPartition, Broker] = Map()
    val rdd = KafkaUtils.createRDD[String, String, StringDecoder, StringDecoder,
      (Long, String, Int, String)](sc, kafkaParams, offsetRanges, map, messageHandler)


    val rowRDD = rdd.map(x => ParseUtils.getRow(x))
    //rowRDD.collect().foreach(println)
    if (!rdd.isEmpty()) {
      val logDF = sqlContext.createDataFrame(rowRDD, ParseUtils.struct)
      logDF.show()
      logDF.printSchema()

      val outputTempLocation = outputPath + "tmp/" + batchid
      logDF.write.format("orc").mode(SaveMode.Overwrite).
        partitionBy("houseid", "dayid", "hourid").save(outputTempLocation)

      moveTempFilesToData(outputPath, batchid)

     val curTimeMil = String.valueOf(System.currentTimeMillis())
     KafkaHbaseManager.saveOffsets(topic, groupName, offsetRanges, hbaseTableName, curTimeMil)
    }


  }

  def moveTempFilesToData(outputPath:String, batchid:String) = {
    val conf = new Configuration
    conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"))
    conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"))
    val fileSystem = FileSystem.get(conf)


    // get Partitions
    val partitionsSet = new mutable.HashSet[String]()
    fileSystem.globStatus(new Path(outputPath + "tmp/" + batchid + "/houseid=*/dayid=*/hourid=*/*.orc")).
      foreach(x=>{
        val fileAbsolutePath = x.getPath.toString
        val fileWithPartition = fileAbsolutePath.replace(fileAbsolutePath.substring(0, fileAbsolutePath.indexOf("/tmp/")) + "/tmp/" + batchid, "")
        val partition = fileWithPartition.substring(0, fileWithPartition.lastIndexOf("/") )
        partitionsSet.add(partition)
      })
    println("partitionsSet:" + partitionsSet)

    // Delete Data Files
    partitionsSet.foreach(p=>{
      fileSystem.globStatus(new Path(outputPath + "data" + p + "/" + batchid + "*.orc")).foreach(
        f=>{
          fileSystem.delete(f.getPath(), false)
        }
      )
    })

    // Move to Data Files
    fileSystem.globStatus(new Path(outputPath + "tmp/" + batchid + "/houseid=*/dayid=*/hourid=*/*.orc")).
      foreach(x=>{
        val fileAbsolutePath = x.getPath.toString
        val fileDir = fileAbsolutePath.substring(0,fileAbsolutePath.lastIndexOf("/"))
        val fileName = fileAbsolutePath.substring(fileAbsolutePath.lastIndexOf("/") + 1)
        val dataDir = fileDir.replace("tmp/" + batchid, "data")
        if(!fileSystem.exists(new Path(dataDir))){
          fileSystem.mkdirs(new Path(dataDir))
        }
        fileSystem.rename(new Path(fileAbsolutePath), new Path(dataDir + "/" + batchid + "-" + fileName))

    })

  }
}
