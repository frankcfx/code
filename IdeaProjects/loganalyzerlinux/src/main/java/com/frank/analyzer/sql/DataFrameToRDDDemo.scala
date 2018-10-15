package com.frank.analyzer.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

object DataFrameToRDDDemo {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建
    val conf = new SparkConf()
      .setMaster("local")
//      .setMaster("spark://spark-master1:7070")
//      .setJars(List("/home/cfx/IdeaProjects/loganalyzerlinux/out/loganalyzerlinux.jar"))
      .setAppName("DataFrameToRDDDemo")
    val sc = SparkContext.getOrCreate(conf)
    // 因为这里需要访问hive表，所以必须使用HiveContext对象作为程序的入口
    val sqlContext = new HiveContext(sc)

    // 读取数据形成DataFrame
    val df = sqlContext.read.table("default.dept")
    df.show()

    // 1. 直接使用rdd属性
    val rdd1: RDD[Row] = df.rdd

    // 2. 使用map或者flatMap API进行转换，等价于: df.rdd.map & df.rdd.flatMap
    val rdd2: RDD[(Int, String, String)] = df.map(row => {
      // Row是DataFrame中的数据类型，Row的数据是以Any的形成存储的，所以在获取数据的时候需要强制转换
      val deptno: Int = row.getInt(0)
      val dname: String = row.getAs[String]("dname")
      val loc = row.getAs[String]("loc")
      (deptno, dname, loc)
    })

    val result = rdd2.collect()
    result.foreach(record => {
      println(record)
    })


  }
}
