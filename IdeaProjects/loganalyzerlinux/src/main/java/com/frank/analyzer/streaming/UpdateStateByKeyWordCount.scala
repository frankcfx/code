package com.frank.analyzer.streaming

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

object UpdateStateByKeyWordCount {

  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local[5]")
      .setAppName("HAKafkaDirectWordCount")
      .set("spark.eventLog.enabled", "true") // 开启日志聚集功能
      .set("spark.eventLog.dir", "hdfs://ns1/spark/history") // 设定日志的聚集的位置(hdfs位置)
      .set("spark.streaming.stopGracefullyOnShutdown", "true") // 设定日志的聚集的位置(hdfs位置)
    val sc = SparkContext.getOrCreate(conf)


    val ssc = new StreamingContext(sc, Seconds(10))
    // 使用updateStateByKey API必须给定checkpoint
    ssc.checkpoint(s"hdfs://ns1/spark/streaming/ha-${System.currentTimeMillis()}")

    // 二、DStream的构建
    val dstream: DStream[String] = ssc.socketTextStream("hive1", 9999)

    // 三、DStream的操作
    /**
      * def updateStateByKey[S: ClassTag](
      * updateFunc: (Seq[V], Option[S]) => Option[S]
      * ): DStream[(K, S)]
      * 对DStream批次中的数据先按照Key进行分组，然后将分组之后的value数据和之前批次执行下来的当前key对应的状态数据进行聚合，使用updateFunc进行聚合
      * updateFunc函数输入参数：
      * Seq[V]：当前批次中相同key的所有value的值
      * Option[S]：之前批次执行后留下的对应key的状态信息，如果状态值不存在，那么该输入参数为None；否则为Some
      * updateFunc函数返回值：
      * Option[S]: 经过当前批次执行后，当前key对应的状态信息，当返回为None的时候，表示不保存该状态信息；如果为Some，表示保留状态信息 ===> 如果一个key的状态不需要保存的话，
      * 那么直接返回None即可
      * updateStateByKey这个API最终返回的DStream中的数据类型是一个二元组，第一个元素的类型其实就是Key的数据类型，第二个元素的类型其实是updateFunc函数返回的状态信息类型
      */
    val result: DStream[(String, Long)] = dstream
      .flatMap(line => line.split(" "))
      .map(word => (word, 1))
      .filter(t => t._1.nonEmpty)
      .reduceByKey((a: Int, b: Int) => a + b)
      .updateStateByKey(
        updateFunc = (seq: Seq[Int], state: Option[Long]) => {
          // 1. 获取当前批次的值
          val currentValue = seq.sum
          // 2. 获取之前批次对应的状态值
          val preValue = state.getOrElse(0L)
          // 3. 返回新的状态值
          Some(currentValue + preValue)
        }
      )

    // 四、DStream结果数据输出
    // 4.1 结果返回driver输出
    result.print()

    // 六、开始运行
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }

}
