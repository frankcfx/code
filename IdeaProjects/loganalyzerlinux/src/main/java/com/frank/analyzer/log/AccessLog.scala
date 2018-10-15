package com.frank.analyzer.log

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

object AccessLog {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("AccessLog")
      //设置Master_IP
      //.setMaster("local")
      .setMaster("spark://spark-master1:7070")
      //提交的jar包在你本机上的位置
      .setJars(List("/home/cfx/IdeaProjects/loganalyzerlinux/target/log-analyzer-1.0-SNAPSHOT.jar"))
      //设置driver端的ip,这里是你本机的ip
      .setIfMissing("spark.eventLog.enabled", "true")
      .setIfMissing("spark.executor.memory", "2g")
      .setIfMissing("spark.eventLog.dir", "hdfs://ns1/spark/history")

    //init context
    //    val sc = new SparkContext(sparkConf)
    val sc = SparkContext.getOrCreate(sparkConf)

    val path = "/user/root/data/access.log"
    val minPartitions = 3
    val rdd = sc.textFile(path, minPartitions)

    val PATTERN = """^(\S+) (\S+) (\S+) \[([\w:/]+\s[+\-]\d{4})\] "(\S+) (\S+) (\S+)" (\d{3}) (\d+)""".r

    val mappedRDD = rdd
      .map(line => PATTERN.findFirstMatchIn(line))
      .filter(res => res.nonEmpty)
      .map(res => {
        val m = res.get
        (
          m.group(1), m.group(2), m.group(3), m.group(4),
          m.group(5), m.group(6), m.group(7), m.group(8).toInt,
          m.group(9).toLong
        )
      })

    mappedRDD.cache()

//    val rddd = sc.parallelize(Array(
//      ("销售部","Tom"), ("销售部","Jack"),("销售部","Bob"),("销售部","Terry"),
//      ("后勤部","Jack"),("后勤部","Selina"),("后勤部","Hebe"),
//      ("人力部","Ella"),("人力部","Harry"),
//      ("开发部","Allen")
//    ))
//    val result = rddd.countByKey();


    val logCountRDD = mappedRDD
      .map( t => ("log", 1))
      .reduceByKey(_ + _)

    val ResponseSizeSumRDD = mappedRDD
      .map(t => ("log", t._9))
      .reduceByKey(_ + _)

    val averageResponseSizeRDD = logCountRDD.join(ResponseSizeSumRDD)
      .map( t => {
        val log = t._1
        val leftValue = t._2._1
        val rightValue = t._2._2
        val averageResponseSize = rightValue/leftValue
        averageResponseSize
      })

    val maxResponseSizeRDD: RDD[(String, ArrayBuffer[Long])] = mappedRDD
      .map( t => ("log", t._9))
      .aggregateByKey(ArrayBuffer[Long]())(
        seqOp = (buf, value) => {
          buf += value
          buf.sorted.takeRight(1)
        },
        combOp = (buf1, buf2) => {
          buf1 ++= buf2
          buf1.sorted.takeRight(1)
        }
      )

    val minResponseSizeRDD = mappedRDD
      .map( t => ("log", t._9))
      .aggregateByKey(ArrayBuffer[Long]())(
        seqOp = (buf, value) => {
          buf += value
          buf.sorted.take(1)
        },
        combOp = (buf1, buf2) => {
          buf1 ++= buf2
          buf1.sorted.take(1)
        }
      )

    val responseCodeRDD = mappedRDD
      .map( t => (t._8, 1))
      .reduceByKey( _ + _)

    val n = 20;
    val ipAccessNTimesRDD = mappedRDD
      .map( t => (t._1, 1))
      .reduceByKey( _ + _)
      .filter( t => t._2 > n)


    val topEndpointRDD = mappedRDD
      .map( t => (t._6, 1))
      .reduceByKey( _ + _)
      .map( t => t.swap)


    val AVGResponseSizeResult = averageResponseSizeRDD.collect()
    AVGResponseSizeResult.foreach(record => {
      println(record)
    })

    val maxResponseSizeResult = maxResponseSizeRDD.collect()
    maxResponseSizeResult.foreach(record => {
      println(record)
    })

    val minResponseSizeResult = minResponseSizeRDD.collect()
    minResponseSizeResult.foreach(record => {
      println(record)
    })

    val responseCodeResult = responseCodeRDD.collect()
    responseCodeResult.foreach(record => {
      println(record)
    })

    val ipAccessNTimesResult = ipAccessNTimesRDD.collect()
    ipAccessNTimesResult.foreach(record => {
      println(record)
    })

    //topEndpointRDD
    val topEndpointResult = topEndpointRDD.top(5)
    topEndpointResult.foreach(record => {
      println(record)
    })

  }
}
