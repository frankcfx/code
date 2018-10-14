package spark88

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 下午7:39.
  */
object SparkHive {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("test").setMaster("local[2]")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)

    // 切换hive的数据库test
    sqlContext.sql("use test2")

    // hive的表
   // sqlContext.sql("insert into testspark select * from default.tmp")

    // 统计
    val df = sqlContext.sql("select id, count(*) as cnt from testspark group by id ")
    df.show()
  }
}
