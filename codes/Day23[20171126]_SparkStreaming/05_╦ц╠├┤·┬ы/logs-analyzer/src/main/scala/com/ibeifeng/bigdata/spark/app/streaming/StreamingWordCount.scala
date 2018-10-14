package com.ibeifeng.bigdata.spark.app.streaming

import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.control.Breaks

/**
  * Created by ibf on 11/26.
  */
object StreamingWordCount {
  def main(args: Array[String]): Unit = {
    // 一、上下文构建
    val conf = new SparkConf()
      .setMaster("local[10]") // 10启动的spark应用可以并行运行10个task任务
      .setAppName("streaming-wordcount")
    val sc = SparkContext.getOrCreate(conf)
    /**
      * batchDuration: 给定创建DStream批次的默认间隔时间；批次的产生和批次的执行时两个不同的线程负责的；当一个批次产生之后，这个批次就会被放入一个待运行的队列中(FIFO)，后台有一个专门的线程负责从队列中获取批次，然后进行批次的运行/调度; 运行/调度的前提：上一个批次执行完成后，才能给从队列中获取当前批次进行执行，如果上一个批次没有执行完成，但是当前批次已经产生了，那么就会导致当前批次延迟执行(等待一段时间) ==> 一般情况下，要求批次的执行时间要比批次的产生间隔时间(batchDuration)小/短
      * 注意：批次与批次之间的数据默认是没有关联关系的，我们在处理数据的时候，默认情况下，只处理一个批次的数据，不需要考虑之前/上一个批次的数据；但是可以通过一些API让批次之间存在关系, eg: xxxByWindow、updateStateByKey
      */
    val ssc = new StreamingContext(sc, Seconds(5))

    // 二、DStream的构建
    val dstream: DStream[String] = ssc.socketTextStream("hadoop-senior01.ibeifeng.com", 9999)

    // 三、DStream的操作
    val result: DStream[(String, Int)] = dstream
      .flatMap(line => line.split(" "))
      .map(word => {
        //        Thread.sleep(1000)
        (word, 1)
      })
      .filter(t => t._1.nonEmpty)
      .reduceByKey((a: Int, b: Int) => a + b)

    // 四、DStream结果数据输出
    // 4.1 结果返回driver输出
    result.print()
    // 4.2 结果输出到HDFS文件系统
    // prefix表示输出到HDFS文件系统的文件夹前缀，suffix表示HDFS文件夹后缀，中间是批次时间
    // 该API的输出结果是，每个批次产生一个文件夹，文件夹中的内容即该批次的执行结果
    result.saveAsTextFiles(prefix = "result/streaming/wc", suffix = "batch")

    // 五、模拟SparkStreaming应用程序的关闭操作
    // 1. 当参数spark.streaming.stopGracefullyOnShutdown设置为true的时候，表示jvm退出的时候，自动进行streamingcontext的关闭操作
    // 2. 代码进行控制关闭对象
    /*new Thread(new Runnable {
      override def run(): Unit = {
        // TODO: 这是一个线程或者是一个其它的对象，在这里进行StreamingContext的关闭操作
        // 1. 是一个循环操作，每隔60s执行一次
        // 2. 首先从外部系统读取消息，当消息为true的时候进行关闭操作
        // 3. 当消息为false的时候，进行休息一段时间后，再次执行循环体
        /*val break = new Breaks
        break.breakable {
          while (true) {
            // 读取外部存储系统中的消息，当消息变成某个值的时候，进行跳出循环操作
            val flag:Boolean = xxx
            if (flag) {
              break.break() // 跳出循环
            }
            Thread.sleep(60000) // 休息一段时间后，再进行检查
          }
        }
        ssc.stop()*/

        var isRunning = true
        while (isRunning) {
          Thread.sleep(20000)
          isRunning = false
        }
        // 调用ssc的stop关闭操作，如果关闭的时候还有batch没有执行，那么会先将等待的batch执行完再进行关闭，但是在此执行过程中，不会产生新的batch
        ssc.stop()
      }
    }).start()*/

    // 六、开始运行
    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate
  }
}
