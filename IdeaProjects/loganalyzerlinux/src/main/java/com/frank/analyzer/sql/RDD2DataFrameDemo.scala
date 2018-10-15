package com.frank.analyzer.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

object RDD2DataFrameDemo {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("hive-join-mysql")
    val sc = SparkContext.getOrCreate(conf)
    // 由于不需要访问Hive，所以使用SQLContext
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    // 1. RDD的构建
    val rdd: RDD[(Int, String)] = sc.parallelize(Array(
      (1, "gerry"),
      (2, "tom"),
      (3, "lili")
    ))

    // 方式一：基于反射的机制
    // 使用class的属性名称作为形成的DataFrame的列名称
    val df = rdd.toDF()
    df.show()
    // 明确给定列名称
    rdd.toDF("id", "name").show()

    // 方式二：明确给定schama信息
    val rdd1: RDD[Row] = rdd.map(t => {
      val id = t._1
      val name = t._2
      val row = Row(id, name)
      row
    })
    val schema = StructType(Array(
      StructField("id", IntegerType),
      StructField("name", StringType)
    ))
    val df3 = sqlContext.createDataFrame(rdd1, schema)
    df3.show()
    df3.toDF("id2", "name2").show()
  }
}