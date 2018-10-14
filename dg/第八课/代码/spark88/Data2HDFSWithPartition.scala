package spark88



import org.apache.spark.sql.{DataFrame, Row, SQLContext, SaveMode}
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.sql.types.{StringType, StructField, StructType}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by hadoop on 下午8:58.
  */
object Data2HDFSWithPartition {

  // 创建Datarame方式2需要
  case class AccessLog(sourceip: String, port: String, url: String, time: String, dayid: String, hourid: String)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[2]").setAppName("test")
    val sc = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sc)

    // 数据源
    val list = List(
      "136.42.33.6,80,http://www.baidu.com,2018-03-22 19:50:32",
      "132.92.73.7,880,http://www.google.com,2018-03-22 19:30:46",
      "138.52.53.22,68,http://www.taobao.com,2018-03-22 18:50:25",
      "192.62.93.56,808,http://www.qq.com,2018-03-22 18:50:24",
      "101.82.33.78,99,http://www.baidu.com,2018-03-22 20:50:14",
      "134.72.23.98,123,http://www.jd.com,2018-03-22 20:20:31"
    )

    // 根据list生成RDD
    val rdd = sc.parallelize(list) //sc.makeRDD(list)
    rdd.take(10).foreach(println)

    // 按日/小时分区


     // 方式1： 转换成dataFrame
   /*   import sqlContext.implicits._
      val rowRDD = rdd.map(line=>getRow(line)).
        map(x=>(x._1,x._2, x._3, x._4, x._5, x._6))
      val df = rowRDD.toDF("sourceip", "port", "url", "time", "dayid", "hourid")
    df.show()*/

    // 方式2： 转换成dataFrame
/*
        val rowRDD = rdd.map(line=>getRow(line))
        import sqlContext.implicits._
        val df = rowRDD.map(x=>AccessLog(x._1,x._2, x._3, x._4, x._5, x._6)).toDF()
        df.show()*/

    // 方式3： 转换成dataFrame
   val rowRDD = rdd.map(x => getRow(x)).map(x => Row(x._1, x._2, x._3, x._4, x._5, x._6))
    val struct = StructType(Array(
      StructField("sourceip", StringType),
      StructField("port", StringType),
      StructField("url", StringType),
      StructField("time", StringType),
      StructField("dayid", StringType),
      StructField("hourid", StringType)
    ))
    val df = sqlContext.createDataFrame(rowRDD, struct)

  // write2HdfsViaHive(sqlContext, df)
    write2HdfsViaDF(df)
  }


  def write2HdfsViaHive(sqlContext: SQLContext, df:DataFrame) = {

    /*
    1. 建表语句
    create external table testlog(sourceip string, port string, url string,
      time string) partitioned by ( dayid string, hourid string)
    stored as orc location '/tmp/sparkhive2';
    2. 开启动态分区
       sqlContext.sql("set hive.exec.dynamic.partition.mode=nonstrict")
    */

    val tmpLogTable = "tmpLog"
    df.registerTempTable(tmpLogTable)

    sqlContext.sql("use test2")
    sqlContext.sql("set hive.exec.dynamic.partition.mode=nonstrict")

    val insertSQL =
      s"""
         |insert into testlog partition(dayid, hourid)
         |select sourceip, port, url, time, dayid, hourid
         |from $tmpLogTable
       """.stripMargin
  sqlContext.sql(insertSQL)

  }


  def write2HdfsViaDF(df:DataFrame) = {
   // df.show(false)
   // df.printSchema()
    val outputPath = "/tmp/sparkdf"
    df.write.format("orc").partitionBy("dayid", "hourid").mode(SaveMode.Overwrite).
      save(outputPath)
  }


  def getRow(line: String) = {

    try {
      val arr = line.split(",")
      val sourceip = arr(0)
      val port = arr(1)
      val url = arr(2)
      val time = arr(3)

      val dayid = time.substring(0, 10).replaceAll("-", "")
      val hourid = time.substring(11, 13)
      (sourceip, port, url, time, dayid, hourid)
    } catch {
      case e: Exception => {
        (line, "-1", "-1", "-1", "-1", "-1")
      }
    }
  }
}
