package com.ibeifeng.bigdata.spark.app.core

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

/**
  * Created by ibf on 11/18.
  */
object PVAndUVSparkCore {
  def main(args: Array[String]): Unit = {
    // 一、创建上下文
    val conf = new SparkConf()
      .setMaster("local") // 如果本地运行，必须指定应用的运行环境， NOTE: 当需要进行集群运行的时候，将该代码注释
      .setAppName("pv&uv") // 给定应用名称
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://hadoop-senior01.ibeifeng.com:8020/spark/history") // 设定日志的聚集的位置(hdfs位置)

    val sc = new SparkContext(conf)

    // 二、RDD的操作
    // 2.1 读取数据形成RDD
    // 给定文件所处的HDFS路径，当给定的路径中没有给定schema的时候，使用默认的文件系统(即fs.defaultFS配置的文件系统)；也就是说当没有集成hdfs的时候，默认文件系统为local
    // minPartitions给定的是形成的RDD的最少分区数量，默认最少为2
//    val path = "datas/page_views.data"
    val path = "hdfs://hadoop-senior01.ibeifeng.com:8020/beifeng/spark/data/page_views.data"
    val minPartitions = 3
    val rdd = sc.textFile(path, minPartitions)

    /**
      * 思路：
      * ---> 数据分为7个字段，业务需要其中的前三个字段(时间、url、用户id)
      * ---> 最终需要的数据格式为: (date, pv, uv)
      * ---> pv的计算和uv的计算是没有关系的，所以先计算pv，再计算uv，最终合并结果
      * ---> 合并结果的时候，考虑如果某一天只有pv，没有uv；那么需要进行默认值填充(0)
      * -1. 将数据过滤出符合条件的数据, 并且只要时间(日期)、url、用户id
      * -2. PV的计算
      * -2.1 select date, count(url) as pv from table where url is not null group by date
      * -2.2 首先对数据做一个过滤，url为空的数据不要；然后做一个分组，最后统计聚合值(url不需要去重)
      * -3. UV的计算
      * -3.1 select date, count(distinct guid) as uv from table where guid is not null group by date
      * -3.2 首先做一个数据过滤、然后做一个数据的分组，最后对每组内的数据进行一个去重计数的操作
      * -3.3 由于在groupByKey之后存在这个迭代器数据的操作，可能存在一些问题，所以尽量更改API，使其不需要使用groupByKeyAPI，所以采用如下方式：先过滤、再去重、再分组、最后计算uv的值
      * -4. PV&UV合并
      * -4.1 使用外连接相关API进行数据join操作，并进行join后的数据处理
      */
    val mappredRDD = rdd
      .map(line => line.split("\t"))
      .filter(arr => arr.length == 7)
      .map(arr => {
        val time = arr(0)
        val date = time.substring(0, math.min(time.length, 10))
        val url = arr(1)
        val guid = arr(2)
        (date, url, guid)
      })

    // 当一个RDD被多次使用，进行cache缓存
    mappredRDD.cache()

    // 2.2 PV的计算
    /*mappredRDD
      .filter(t => t._2.nonEmpty)
      .map(t => (t._1, t._2))
      .groupByKey()
      .map(t => {
        val date = t._1
        val urls = t._2
        // 计算url的数量 ==> pv的值
        val pv = urls.size
        (date, pv)
      })
    mappredRDD
      .filter(t => t._2.nonEmpty)
      .map(t => (t._1, 1))
      .groupByKey()
      .map(t => {
        val date = t._1
        // 计算url的数量 ==> pv的值
        val pv = t._2.reduce((a,b) => a + b)
        (date, pv)
      })*/
    val pvRDD: RDD[(String, Int)] = mappredRDD
      .filter(t => t._2.nonEmpty)
      .map(t => (t._1, 1))
      .reduceByKey(_ + _)
    // 2.3 UV的计算
    /*mappredRDD
      .filter(t => t._3.nonEmpty)
      .map(t => (t._1, t._3))
      .groupByKey()
      .map(t => {
        val date = t._1
        val guids: Iterable[String] = t._2
        // 计算guid的去重后的数量 ==> uv的值
        val uv = guids.toSet.size
        (date, uv)
      })

    mappredRDD
      .filter(t => t._3.nonEmpty)
      .map(t => (t._1, t._3))
      .map(t => (t, 1))
      .groupByKey()
      .map(t => {
        // 因为是一个去重操作，所以一组数据中，只需要一条数据
        t._1
      })
    mappredRDD
      .filter(t => t._3.nonEmpty)
      .map(t => (t._1, t._3))
      .map(t => (t, 1))
      .reduceByKey((a, b) => a)
      .map(t => t._1)*/
    val uvRDD: RDD[(String, Int)] = mappredRDD
      .filter(t => {
        t._3.nonEmpty
      })
      .map(t => {
        (t._1, t._3)
      })
      .distinct() // 数据去重，当数据完全一样的时候，就去掉重复数据
      .map(t => (t._1, 1)) // 因为数据去重后，计算uv不需要考虑guid的值，所以使用1来替换
      .reduceByKey(_ + _)

    // 2.4 数据合并
    val pvAndUvRDD = pvRDD
      .fullOuterJoin(uvRDD)
      .map(t => {
        val date = t._1
        val pvOption: Option[Int] = t._2._1
        val uvOption: Option[Int] = t._2._2
        (date, pvOption.getOrElse(0), uvOption.getOrElse(0))
      })

    // 2.5 数据输出（返回driver进行输出、executor中进行数据输出、保存HDFS....）
    // 1. 返回driver(eg: collect、take、first、top....)
    val result: Array[(String, Int, Int)] = pvAndUvRDD.collect()
    println("result 1")
    result.foreach(record => {
      println("result 2")
      println(record)
    })
    // 2. 直接executor中输出(eg: foreach、foreachPartition)
    pvAndUvRDD.foreachPartition(iter => {
      // TODO: 对一个分区的数据结果进行一个输出操作，具体的输出代码自定义;iter即当前分区的结果迭代器
      println("result 3")
      iter.foreach(record => {
        println("result 4")
        println(record)
      })
    })
    // 3. 直接输出到HDFS文件系统
    // NOTE: 和数据的读取一样，当给定的文件夹路径中没有给定schema信息的时候，表示使用默认的文件系统(即dfs.defaultFS配置的文件系统)
    pvAndUvRDD.saveAsTextFile(s"/beifeng/spark/core/pv_uv/${System.currentTimeMillis()}")


    // 休息一段时间，方便查看4040页面
    Thread.sleep(1000)
  }
}
