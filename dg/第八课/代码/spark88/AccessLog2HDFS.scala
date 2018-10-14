package spark88

import java.text.SimpleDateFormat
import java.util.Base64

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{IntegerType, StringType, StructField, StructType}
import org.apache.spark.sql.{Row, SaveMode}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 下午8:58.
  * 建表语句：
  * create external table accesslog
    (
    srcip string, destip string, proctype string, srcport string, destport string, domain string, url string, duration string, acctime string
    ) partitioned by (houseid string, dayid string, hourid string)
    stored as orc location '/hadoop/accesslog/';
  */
object AccessLog2HDFS {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("test")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)


    val rowRdd = sc.textFile("/tmp/srcdata/accesslog.txt").map(x=>getRow(x))
    val df = sqlContext.createDataFrame(rowRdd, struct)
    //df.show()
    //df.printSchema()

    val tempAccessTable = "tempAccesslog"
    df.registerTempTable(tempAccessTable)

    // 开启hive的动态分区
    sqlContext.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    // 切换到test库
    sqlContext.sql("use test11")

    // 按照分区条件入不同的hdfs目录
    val insertSql =
      s"""
         |insert into accesslog partition(houseid, dayid, hourid)
         |select srcip, destip, proctype, srcport, destport, domain,
         |       url, duration, acctime, houseid, dayid, hourid
         |from $tempAccessTable
       """.stripMargin

    sqlContext.sql(insertSql)
    //df.write.format("orc").partitionBy("houseid", "dayid", "hourid").mode(SaveMode.Append).save("xx")

  }

  def getTime(time: String): Tuple3[String, String, String] = {
    try {
      val sdf = new SimpleDateFormat("yyyyMMddHHmmss")
      val timeStr = sdf.format(time.toLong * 1000)
      val dayid = timeStr.substring(2, 8).replaceAll("-", "")
      val hourid = timeStr.substring(8, 10)
      (timeStr, dayid, hourid)
    } catch {
      case e: Exception => {
        ("0", "0", "0")
      }
    }
  }

  val struct = StructType(Array(
    StructField("houseid", IntegerType),
    StructField("srcip", StringType),
    StructField("destip", StringType),
    StructField("proctype", StringType),
    StructField("srcport", StringType),

    StructField("destport", StringType),
    StructField("domain", StringType),
    StructField("url", StringType),
    StructField("duration", StringType),
    StructField("acctime", StringType),

    StructField("dayid", StringType),
    StructField("hourid", StringType)
  ))

  /**
    *
    * @param line
    * @return Row
    */
  def getRow(line: String) = {
    val msg = line
    try {
      val arr = msg.split("\\|", 10)
      var houseid = 0
      val timeTuple = getTime(arr(9))
      try {
        houseid = arr(0).toInt
      } catch {
        case e: Exception => {
        }
      }

      var url = arr(7)
      try {
        val urlBytes = Base64.getDecoder.decode(arr(7))
        url = new String(urlBytes, "utf-8")
      } catch {
        case e: Exception => {
        }
      }
      Row(houseid, arr(1), arr(2), arr(3), arr(4), arr(5), arr(6), url, arr(8), timeTuple._1, timeTuple._2, timeTuple._3)
    } catch {
      case e: Exception => {
        val houseid = -1
        Row(houseid, line, "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1")
      }
    }
  }
}
