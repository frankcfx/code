package spark55.phoenix

import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.phoenix.spark._

/**
  * create table out_test_table (id bigint not null primary key, col1 varchar, col2 integer );
  */
object SparkReadPhoenixDF {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("testPhonix")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val dataset = List(("1", "1", "1"), ("2", "2", "2"), ("3", "3", "3"))

    sc.parallelize(dataset).saveToPhoenix("out_test_table", Seq("id", "col1", "col2"), zkUrl = Some("zookeeper1:2181"))
  }

}
