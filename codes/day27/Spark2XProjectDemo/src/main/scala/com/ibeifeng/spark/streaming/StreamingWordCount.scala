package com.ibeifeng.spark.streaming

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.streaming.OutputMode

/**
  * Created by ibf on 11/05.
  */
object StreamingWordCount {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建 ==> SparkSession构建
    val spark = SparkSession
      .builder()
      .appName("demo")
      .master("local[10]")
      .config("spark.sql.shuffle.partitions", "10")
      .getOrCreate()
    import spark.implicits._

    // 二、构建
    val lines = spark
      .readStream
      .format("socket")
      .option("host", "192.168.187.146")
      .option("port", 9999)
      .load()

    // 三、数据处理、业务代码编写
    val wordCount = lines
      .as[String]
      .flatMap(_.split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .groupByKey(t => t._1)
      /*.mapGroups((word, iter) => {
        // 求出现的总数量
        val count = iter.map(_._2).sum
        (word, count)
      })*/
      .count()

    // 四、获取一个输出的计划（query类似StreamingContext对象）
    val query = wordCount
      .writeStream
      .format("console")
      .outputMode(OutputMode.Complete())
      .start()

    // 五、等待执行完成
    query.awaitTermination()
    //    query.stop()
  }
}
