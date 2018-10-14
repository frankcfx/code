package spark11.rdbms

import java.util.Properties

import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 上午1:49.
  */
object LoadMySQL {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("LoadMySQL")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    var jdbcDf=sqlContext.read.format("jdbc").options(Map("url"->"jdbc:mysql://localhost:3306/myspark",
      "driver" -> "com.mysql.jdbc.Driver",
      "dbtable"->"mytopic",
      "user"->"root","password"->"123456")).load()

    jdbcDf.show
  }
}
