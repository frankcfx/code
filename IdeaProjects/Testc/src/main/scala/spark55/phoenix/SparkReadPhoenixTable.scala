package spark55.phoenix

import org.apache.hadoop.conf.Configuration
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.phoenix.spark._

object SparkReadPhoenixTable {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("testPhonix")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)


    val conf = new Configuration
    conf.set("hbase.zookeeper.quorum", "zookeeper1:2181")

    val df = sqlContext.phoenixTableAsDataFrame(
      "WEB_STAT",
      Array("HOST", "DOMAIN"),
      conf = conf
    )

    df.filter(df("HOST") === "EU").show
  }

}
