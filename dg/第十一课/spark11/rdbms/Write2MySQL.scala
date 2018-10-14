package spark11.rdbms

import java.util.Properties

import org.apache.spark.sql.{SQLContext, SaveMode}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 上午1:49.
  */
object Write2MySQL {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("LoadMySQL")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)

    // 生成DataFrame
    val list = List(("mykafka", 0, 2345), ("mykafka", 1, 2325), ("mykafka", 1, 2378) )
    import sqlContext.implicits._
    val df = sc.makeRDD(list).toDF("topic", "partid", "offset")
    df.show()

    // 将DataFrame数据写入mysql
    val prop=new Properties()
    prop.setProperty("user","root")
    prop.setProperty("password","123456")
    val url = "jdbc:mysql://localhost:3306/myspark"
    df.write.mode(SaveMode.Append).jdbc(url,"my_kafka",prop)

  }
}
