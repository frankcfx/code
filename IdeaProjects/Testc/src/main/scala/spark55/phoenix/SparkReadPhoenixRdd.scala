package spark55.phoenix

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.phoenix.spark._
import org.apache.spark.rdd.RDD

object SparkReadPhoenixRdd {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("testPhonix")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val rdd: RDD[Map[String, AnyRef]] = sc.phoenixTableAsRDD(
      "WEB_STAT",
      Seq("HOST", "DOMAIN"),
      zkUrl = Some("zookeeper1:2181")
    )

    println(rdd.count())

    val host = rdd.first()("HOST").asInstanceOf[String]
    val domain = rdd.first()("DOMAIN").asInstanceOf[String]

    println("host: " + host)
    println("domain: " + domain)

    rdd.take(10).foreach(println)

  }

}
