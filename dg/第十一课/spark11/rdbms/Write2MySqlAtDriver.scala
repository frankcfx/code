package spark11.rdbms

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

/**
  * Created by zhoucw on 上午2:09.
  */
class Write2MySqlAtDriver {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("LoadMySQL")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    // 生成DataFrame
    val list = List(("mykafka", 0, 2345), ("mykafka", 1, 2325), ("mykafka", 1, 2378) )
    import sqlContext.implicits._
    val df = sc.makeRDD(list).toDF("topic", "partid", "offset")




  }
}
