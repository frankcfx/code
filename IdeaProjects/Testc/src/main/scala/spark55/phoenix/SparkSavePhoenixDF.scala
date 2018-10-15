package spark55.phoenix

import org.apache.spark.sql.{DataFrame, SQLContext, SaveMode}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 创建表,注意info.companyname 这里的info是列族名
  * create table test_company (areacode varchar, code varchar, info.companyname varchar constraint pk primary key (areacode, code));
  */
object SparkSavePhoenixDF {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("testPhonix")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    val dataSet = List(("021", "54657", "Apple.com"), ("023", "64780", "Hp.com"), ("010", "23567", "Google.com"))
    import sqlContext.implicits._

    val df: DataFrame = sc.parallelize(dataSet).map(x => (x._1, x._2, x._3)).toDF("areacode", "code", "companyname")

    df.write.format("org.apache.phoenix.spark")
      .mode(SaveMode.Overwrite)
      .options(Map("table" -> "test_company", "zkUrl" -> "zookeeper1:2181"))
      .save()

  }

}
