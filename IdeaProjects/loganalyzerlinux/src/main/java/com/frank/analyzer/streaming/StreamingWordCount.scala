package com.frank.analyzer.streaming

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}

object StreamingWordCount {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local[5]")
      .setAppName("StreamingWordCount")
//      .set("spark.sql.shuffle.partitions", "10") // sparksql的默认分区数为200个
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
      .set("spark.streaming.stopGracefullyOnShutdown", "true") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)

    val ssc = new StreamingContext(sc, Seconds(5))

    val ds:DStream[String] = ssc.socketTextStream("hive1",9999)
//    val ds:DStream[String] = ssc.socketTextStream("spark-master1",9999)

    val result: DStream[(String, Int)] = ds.flatMap(line => line.split(" "))
      .map(word => word.toLowerCase())
      .filter(word => word.nonEmpty)
      .map(word => (word,1))
      .reduceByKey((a,b) => a + b)

    result.print()
    // 输出到外部的存储系统(HDFS)《要求hdfs文件夹不存在》
    result.saveAsTextFiles(prefix = "/frank/spark/streaming/wc", suffix = "batch")

    ssc.start()
    ssc.awaitTermination()

  }

}
