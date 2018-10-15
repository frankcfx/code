package com.test

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

object TestC {

  def main(args: Array[String]): Unit = {

//    val sparkConf = new SparkConf().setAppName("test").setMaster("local[2]")
    val sparkConf = new SparkConf().setAppName("test")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)

    val sql =
      """
        |select * from test.testtable
      """.stripMargin

    sqlContext.sql(sql).show()
  }

}
