package com.frank.analyzer.log

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object PVAndUVSparkCore {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("SparkCorePVUV")
      //设置Master_IP
      .setMaster("local")
//      .setMaster("spark://beifeng:7070")
//      //提交的jar包在你本机上的位置
      .setJars(List("/home/cfx/IdeaProjects/loganalyzerlinux/target/log-analyzer-1.0-SNAPSHOT.jar"))
      //设置driver端的ip,这里是你本机的ip
      .setIfMissing("spark.eventLog.enabled", "true")
      .setIfMissing("spark.executor.memory", "2g")
      .setIfMissing("spark.eventLog.dir", "hdfs://ns1/spark/history")

    //init context
//    val sc = new SparkContext(sparkConf)
    val sc = SparkContext.getOrCreate(sparkConf)

    //date guid url
    //UV
    //select date, count(distinct guid) as uv from table where guid is not null group by date
    //select date, count (url) as pv from table where url is not null group by date
    //select date, count (1) as pv from table where url is not null group by date 可以替换上面的语句

    //select case when temp1.date == null then temp2.date else temp1.date end as date, temp2.pv, temp1.uv
    // from (select date, count(distinct guid) as uv from table where guid is not null group by date) as temp1
    // full join (select date, count (1) as pv from table where url is not null group by date) as temp2
    // on temp1.date =temp2.date

    val path = "/user/root/data/page_views.data"
    val minPartitions = 3
    val rdd = sc.textFile(path, minPartitions)

    val mappredRDD = rdd
      .map(line => line.split("\t"))
      .filter(arr => arr.length == 7)
      .map(
        arr => {
          val time = arr(0)
          val date = time.substring(0, Math.min(time.length, 10))
          val url = arr(1)
          val guid = arr(2)
          (date, url, guid)
        }
      )

//    mappredRDD
//      .filter( t => t._2.nonEmpty)
//      .map(t => (t._1,t._2))
//      .groupByKey()
//      .map(
//        t => {
//          val date = t._1
//          val urls = t._2
//          val pv = urls.size
//
//          (date, pv)
//        }
//      )

    val pvRDD: RDD[(String, Int)] = mappredRDD
      .filter( t => t._2.nonEmpty)
      .map(t => (t._1, 1))
//      .reduceByKey((a,b) => a+b)
      .reduceByKey(_ + _)

//        mappredRDD
//          .filter( t => t._2.nonEmpty)
//          .map(t => (t._1,t._3))
//          .groupByKey()
//          .map(
//            t => {
//              val date = t._1
//              val guids = t._2
//              val uv = guids.toSet.size
//              (date, uv)
//            }
//          )

    val uvRDD: RDD[(String, Int)] = mappredRDD
      .filter( t => t._3.nonEmpty )
      .map(t => (t._1, t._3))
      .distinct()
      .map(t => (t._1, 1))
      .reduceByKey(_ + _)

    val pvAndUvRDD = pvRDD
      .fullOuterJoin(uvRDD)
      .map( t => {
        val date: String = t._1
        val pvOption: Option[Int] = t._2._1
        val uvOption: Option[Int] = t._2._2
        (date, pvOption.getOrElse(0),uvOption.getOrElse(0))
      })

    //数据输出 ：1返回driver输出，2.在executor中输出，包括保存在hdfs中

    //1. 返回driver api包括：collect top first take
    val result: Array[(String, Int, Int)] = pvAndUvRDD.collect()
    println("result 1")
    result.foreach(record => {
      println("result 2")
      println(record)
    })

    //2. 在executor中输出 api包括：foreach foreachPartition
    //foreachPartition 对一个分区的数据结果进行一个操作，iter即当前分区的结果的迭代器
    pvAndUvRDD.foreachPartition(iter => {
      println("result 3")
      iter.foreach( record => {
        println("result 4")
        println(record)
      })
    })

    //3. 输出到HDFS中
    pvAndUvRDD.saveAsTextFile(s"/user/root/spark/core/${System.currentTimeMillis()}")

    sc.stop()
  }
}