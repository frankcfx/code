package com.ibeifeng.bigdata.spark.app.sql

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, SQLContext}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ibf on 11/25.
  */
case class DatasetPerson(id: Int, name: String)

object DatasetDemo {
  def main(args: Array[String]): Unit = {
    // 一、上下午构建
    val conf = new SparkConf()
      .setAppName("DatasetDemo")
      .setMaster("local[*]")
    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    // 二、Dataset构建方式一：利用反射机制进行构建
    val ds1: Dataset[Int] = Seq(1, 2, 3).toDS()
    val ds2: Dataset[Tuple1[Int]] = sc.parallelize(Array(1, 2, 3, 4, 5).map(t => Tuple1.apply(t))).toDS()

    // 三、Dataset创建方式二：将DataFrame转换为Dataset
    val df: DataFrame = sc.parallelize(Array(
      (1, "gerry"),
      (2, "tom"),
      (3, "lili")
    )).toDF("id", "name")
    // 如果DataFrame中存在null，那么转换会失败
    // 要求DataFrame中的schame信息和给定的case class对象中的属性必须完全一致(顺序&类型&名称)
    val ds3: Dataset[DatasetPerson] = df.as[DatasetPerson]
    ds3.show()

    // Dataset => RDD
    val rdd1: RDD[DatasetPerson] = ds3.rdd
    // Dataset => DataFrame
    val df1 = ds3.toDF()

    // Dataset上的API操作 ==> 比较类似RDD的API
    ds3
      .map(t => {
        val id = t.id
        (id, t.name, 18)
      })
      .filter(t => t._2 == "gerry")
      .show()

    ds3
      .toDF()
      .map(row => {
        val id: Long = row.getAs[Int]("id")
        val name = row.getAs[String]("name")
        (id, name, 18)
      })
      .filter(t => t._2 == "gerry")
      .collect()

  }
}
