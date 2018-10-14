package com.ibeifeng.bigdata.spark.app.sql

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}

/**
  * Created by ibf on 11/25.
  */
object ReadCSVSparkSQL {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("ReadCSVSparkSQL")
      .set("spark.sql.shuffle.partitions", "20") // sparksql的默认分区数为200个
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://hadoop-senior01.ibeifeng.com:8020/spark/history") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)
    // 由于使用了ROW_NUMBER函数，所以这里必须使用HiveContext
    val sqlContext = new HiveContext(sc)
    import sqlContext.implicits._

    sqlContext.udf.register("self_min", (a: Int, b: Int) => if (a > b) b else a)

    // 2. 读取CSV文件的数据，形成DataFrame
    val path = "datas/taxi.csv"

    /**
      * 读取数据的方式：
      * 方式一：读取数据形成RDD，然后转换为DataFrame
      * sc.textFile(path)
      * .map(line => line.split(","))
      * .filter(arr => arr.length == 4)
      * .map(arr => (arr(0), arr(1), arr(2), arr(3)))
      * .toDF("id", "lat", "lon", "time")
      * 方式二：通过第三方的插件读取数据形成DataFrame
      * https://github.com/databricks/spark-csv
      */
    val schema = StructType(Array(
      StructField("id", IntegerType),
      StructField("lat", StringType),
      StructField("lon", StringType),
      StructField("time", StringType)
    ))
    val df = sqlContext
      .read
      .format("com.databricks.spark.csv") // 给定数据如何读取
      .schema(schema) // 给定数据的schema信息，可以不给定
      .option("header", "false") // header参数的含义是指第一行是否是列名称，默认为false
      .load(path)
    df.registerTempTable("tmp_taxi")
    df.show()

    // 3. 业务代码的编写
    // 3.1 获取某个出租车在某个时间点(小时)的载客信息
    sqlContext
      .sql(
        """
          |SELECT
          |  id, substring(time, 0, self_min(2, length(time))) as hour
          |FROM tmp_taxi
        """.stripMargin)
      .registerTempTable("tmp01")
    // 3.2 统计各个出租车在各个时间点的载客次数
    sqlContext
      .sql(
        """
          |SELECT
          |  id, hour, COUNT(1) AS cnt
          |FROM tmp01
          |GROUP BY id, hour
        """.stripMargin)
      .registerTempTable("tmp02")
    // 3.3 对所有数据进行分组，然后对每组数据进行排序 ==> ROW_NUMBER
    sqlContext
      .sql(
        """
          |SELECT
          |  id, hour, cnt,
          |  ROW_NUMBER() OVER (PARTITION BY hour ORDER BY cnt DESC) as rnk
          |FROM tmp02
        """.stripMargin)
      .registerTempTable("tmp03")
    // 3.4 获取每组中rnk最大的前5的数据
    sqlContext
      .sql(
        """
          |SELECT
          |  id, hour, cnt
          |FROM tmp03
          |WHERE rnk <= 5
        """.stripMargin)
      .registerTempTable("tmp_result01")

    // 四、结果数据的输出
    sqlContext
      .read
      .table("tmp_result01")
      .repartition(5)
      .write
      .format("com.databricks.spark.csv") // 给定写出的数据类型
      .option("header", "true")
      .mode(SaveMode.Overwrite) // 指定当文件夹存在的时候进行任何操作，overwrite表示覆盖 ==> 先删除文件夹，在重新构建
      .save("result/csv")

  }
}
