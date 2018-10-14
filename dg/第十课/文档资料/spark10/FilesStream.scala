package spark10

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{LongWritable, Text}
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}

/**
  * Created by hadoop on 上午1:38.
  */
object FilesStream {
  def main(args: Array[String]): Unit = {
    // 参数接收监控到目录
    if (args.length < 1) {
      System.err.println("Usage: <log-dir>")
      System.exit(1)
    }

    // 监控目录
    val inputDirectory = args(1)

    val sparkConf = new SparkConf()
    val ssc = new StreamingContext(sparkConf, Seconds(30))

    // 监控目录过滤以.uploading结尾的文件
    def uploadingFilter(path: Path): Boolean = !path.getName().endsWith("._COPYING_") &&
      !path.getName().endsWith(".uploading")

    val lines = ssc.fileStream[LongWritable, Text, TextInputFormat](inputDirectory,uploadingFilter(_), false).
      map(p => new String(p._2.getBytes, 0, p._2.getLength, "GBK"))
    lines.foreachRDD(rdd=>{
      rdd.foreachPartition(p=>{
        println(p)
      })
    })

    ssc.start()
    ssc.awaitTermination()
  }
}
