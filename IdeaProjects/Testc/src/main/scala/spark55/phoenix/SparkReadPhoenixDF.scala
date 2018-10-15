package spark55.phoenix

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

object SparkReadPhoenixDF {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("testPhonix")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val df = sqlContext.read
      .format("org.apache.phoenix.spark")
      .options(Map("table" -> "WEB_STAT", "zkUrl" -> "zookeeper1:2181"))
      .load()

    df.filter(df("HOST") === "EU").show
  }

}
