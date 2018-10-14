package spark11.rdbms

import java.sql.{Connection, PreparedStatement}
import java.util

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.SaveMode
import org.apache.spark.{SparkConf, SparkContext}
import spark11.rdbms.utils.DbUtil.getDBConnection

/**
  * 从MySQL读取数据， MySQL服务器与Hadoop集群网络不通
  * 1. Spark 程序提交使用yarn-client模式
  * 2. Spark客户端与MySQL服务器和Hadoop集群网络连通
  * 3. 使用单机模式， 从MySQL读取数据， 将数据保存在ArrayList中
  * 4. 使用MakeRDD或者parallalize 将数据转换成RDD分布到集群中
  * Created by hadoop on 上午1:49.
  */
object LoadMySQL2HDFS {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("LoadMySQL")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)

    var dbConnection: Connection = null
    var preparedStatement: PreparedStatement = null
    val sql = "select topic, partid, offset from my_test"
    val result = new util.ArrayList[Tuple3[String, Int, Long]]()

    try {
      dbConnection = getDBConnection
      preparedStatement = dbConnection.prepareStatement(sql)
      // execute select SQL stetement
      val rs = preparedStatement.executeQuery()
      while (rs.next()) {
        result.add((rs.getString(1), rs.getInt(2), rs.getLong(3)))
      }
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    } finally {
      if (preparedStatement != null) {
        preparedStatement.close()
      }
      if (dbConnection != null) {
        dbConnection.close()
      }
    }


    val rdd = sc.makeRDD(result.toArray()).asInstanceOf[RDD[Tuple3[String, Int, Long]]]
    import sqlContext.implicits._
    val df = rdd.map(x=>(x._1, x._2, x._3)).toDF("topic", "partid", "offset")
    df.write.format("orc").mode(SaveMode.Overwrite).save("/tmp/dbdata")

  }
}
