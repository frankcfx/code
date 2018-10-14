package spark11.rdbms

import java.sql.{Connection, PreparedStatement}
import java.util

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}
import spark11.rdbms.utils.DbUtil.getDBConnection

/**
  * 从MySQL读取数据， MySQL服务器与Hadoop集群网络不通
  * 1. Spark 程序提交使用yarn-client模式
  * 2. Spark客户端与MySQL服务器和Hadoop集群网络连通
  * 3. 使用collect算子， 将数据拉取到driver端
  * 4. 在driver端（hadoop客户端）将数据写入到MySQL
  * Created by hadoop on 上午1:49.
  */
object LoadHDFS2MySQL {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("LoadMySQL")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)

    val df = sqlContext.read.format("orc").load("/tmp/dbdata")

    val result = df.map(x=>(x.getString(0), x.getInt(1), x.getLong(2))).collect()


    var dbConnection: Connection = null
    var pstmt: PreparedStatement = null
    val sql = "insert into my_test(topic, partid, offset) values (?,?,?)"
    try {
      dbConnection = getDBConnection
      dbConnection.setAutoCommit(false)
      pstmt = dbConnection.prepareStatement(sql)

      var i = 0
      for (r <- result) {
        val topic = r._1
        val partid = r._2
        val offset = r._3

        pstmt.setString(1, topic)
        pstmt.setInt(2, partid)
        pstmt.setLong(3, offset)
        i += 1
        pstmt.addBatch()
        if (i % 1000 == 0) {
          pstmt.executeBatch
        }
      }
      pstmt.executeBatch

      dbConnection.commit()
      pstmt.close()
      dbConnection.close()
    } catch {
      case e: Exception => {
        e.printStackTrace()
      }
    } finally {
      if (pstmt != null) {
        pstmt.close()
      }
      if (dbConnection != null) {
        dbConnection.close()
      }
    }

  }
}
