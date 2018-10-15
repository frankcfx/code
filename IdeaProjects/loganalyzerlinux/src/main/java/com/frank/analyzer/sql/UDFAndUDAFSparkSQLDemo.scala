package com.frank.analyzer.sql


import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random
object UDFAndUDAFSparkSQLDemo {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("UDFAndUDAFSparkSQLDemo")
      .set("spark.sql.shuffle.partitions", "10") // sparksql的默认分区数为200个
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)
    val sqlContext = new SQLContext(sc)
    import sqlContext.implicits._

    // 自定义函数：一般写在SQLContext构建完成的地方
    // udf定义：sqlContext.udf.register(函数名称，函数处理代码/对象<函数对象，一般为匿名函数>)
    sqlContext.udf.register("format_double", (value: Double) => {
      // NOTE: 函数的返回值必须是可以进行序列化的
      import java.math.BigDecimal
      val bd = new BigDecimal(value)
      bd.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue()
    })
    // UDAF：sqlContext.udf.register(函数名称，UserDefinedAggregateFunction的一个子类的对象)
    sqlContext.udf.register("self_avg", SelfAVGUDAF)


    // 二、RDD构建并转换为DataFrame，并将DataFrame注册成为临时表
    val random = Random
    val rdd: RDD[(Int, Double)] = sc.parallelize(
      (0 to 10)
        .flatMap(i => {
          (0 to (random.nextInt(10) + 2)).map(j => i)
        })
        .map(i => {
          // 随机一个value值
          (i, random.nextDouble() * 1000 + 10)
        })
    )
    val df = rdd.toDF("id", "sal")
    df.registerTempTable("tmp01")
    df.show()

    // 需求：对每个部门求解sal的平均值
    // 并且对均值的结果进行格式化操作，四舍五入，保留两位小数
    sqlContext
      .sql(
        """
          |SELECT
          |  id,
          |  AVG(sal) AS sal1,
          |  format_double(AVG(sal)) AS sal2,
          |  self_avg(sal) AS sal3,
          |  format_double(self_avg(sal)) AS sal4
          |FROM tmp01
          |GROUP BY id
        """.stripMargin)
      .show()

  }
}