package com.frank.analyzer.log

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

object Loganalyzer {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("SparkTest")

      //    conf.set("mapreduce.app-submission.cross-platform", "true")
      //设置Master_IP
      .setMaster("spark://beifeng:7070")
      //提交的jar包在你本机上的位置
      .setJars(List("/home/cfx/IdeaProjects/loganalyzerlinux/target/log-analyzer-1.0-SNAPSHOT.jar"))
      //设置driver端的ip,这里是你本机的ip
      //      .setIfMissing("spark.driver.host", "192.168.50.1")
      .setIfMissing("spark.eventLog.enabled", "true")
      .setIfMissing("spark.eventLog.dir", "hdfs://ns1/spark/history")




    val sc = new SparkContext(sparkConf)

    println("SparkTest...")

    val lines = sc.textFile("/user/root/data/word.txt")
    val result2: RDD[(String, Int)] = lines
      .flatMap(line => line.split(" "))
      .map(word => word.toLowerCase())
      .filter(word => word.nonEmpty)
      .map(word => (word,1))
      .reduceByKey((a,b) => a + b)

    val topresult: Array[(Int, String)] = result2.map(t => t.swap).top(20)

    topresult.map( r => println(r))

    val topresult2 = result2.map(t => t.swap).first()

    sc.stop
  }

}
