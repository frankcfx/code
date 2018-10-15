package com.frank.analyzer.sql

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

object ReadCSVSparkSQL {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("ReadCSVSparkSQL")
      .set("spark.sql.shuffle.partitions", "10") // sparksql的默认分区数为200个
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new HiveContext(sc)
//    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    sqlContext.udf.register("self_min", (a:Int, b:Int) => if(a > b) b else a )

    val path = "/user/root/data/taxi.csv"
//    val path = "datas/taxi.csv"

//    sc.textFile(path)
//      .map(line => line.split(","))
//      .filter(arr => arr.length == 4)
//      .map(arr => (arr(0), arr(1), arr(2), arr(3)))
//      .toDF("id", "lat", "lon", "time")
//    方式二：通过第三方的插件读取数据形成DataFrame
//    https://github.com/databricks/spark-csv

    val customSchema = StructType(Array(
      StructField("id", IntegerType, true),
      StructField("lat", StringType, true),
      StructField("lon", StringType, true),
      StructField("time", StringType, true)))

    val df = sqlContext.read
      .format("com.databricks.spark.csv")
      .option("header", "false") // Use first line of all files as header
      .schema(customSchema)
      .load(path)

    df.registerTempTable("tmp_taxi")
//    df.show

    sqlContext.sql(
      """
        |SELECT id, substring(time, 0, self_min(2, length(time))) as hour
        |FROM tmp_taxi
      """.stripMargin)
      .registerTempTable("tmp_hour")

//    sqlContext.sql(
//      """
//        |SELECT *
//        |FROM tmp_hour
//      """.stripMargin)
//      .show

    sqlContext.sql(
      """
        |SELECT id, hour, count(1) as cnt
        |FROM tmp_hour
        |GROUP BY id, hour
      """.stripMargin)
      .registerTempTable("tmp_hour_01")

    sqlContext.sql(
      """
        |SELECT id, hour,  cnt, ROW_NUMBER() OVER (PARTITION BY hour ORDER BY cnt DESC) as rnk
        |FROM tmp_hour_01
      """.stripMargin)
      .registerTempTable("tmp_hour_02")

    sqlContext.sql(
      """
        |SELECT id, hour, cnt, rnk
        |FROM tmp_hour_02
        |WHERE rnk <= 5
      """.stripMargin)
      .registerTempTable("tmp_hour_03")

    sqlContext
      .read
      .table("tmp_hour_03")
        .repartition(2)
      .write
      .format("com.databricks.spark.csv")
      .option("header", "true")
        .mode(SaveMode.Overwrite)
      .save("/user/root/out/taxi.csv")


//    sqlContext.sql((
//      """
//        |SELECT *
//        |FROM tmp_hour_01
//      """.stripMargin)).show(2000,false)
//
//    sqlContext.sql((
//      """
//        |SELECT *
//        |FROM tmp_hour_02
//      """.stripMargin)).show(2000,false)

  }

}
