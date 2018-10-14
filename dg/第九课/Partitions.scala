package spark99

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 上午1:33.
  */
object Partitions {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("test").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val list = List(1, 2, 3)
    val rdd = sc.parallelize(list)
    //rdd.repartition(1)
    //rdd.coalesce(1)

    println("rdd默认分区数: " + rdd.getNumPartitions)
    println("rdd.coalesce(4): " + rdd.coalesce(4).getNumPartitions)
    println("rdd.repartition(4): " + rdd.repartition(4).getNumPartitions)
    println("rdd.coalesce(1): " + rdd.coalesce(1).getNumPartitions)
    println("rdd.repartition(1): " + rdd.repartition(1).getNumPartitions)

  }
}
