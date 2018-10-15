package com.frank.analyzer.sql

import java.util.Properties

import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

object HiveJoinMySQLDataSparkSQL {

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("HiveJoinMySQLDataSparkSQL")
//      .setMaster("spark://spark-master1:7070")
//      .setJars(List("/home/cfx/IdeaProjects/loganalyzerlinux/out/loganalyzerlinux.jar"))
//      .setIfMissing("spark.eventLog.enabled", "true")
//      .setIfMissing("spark.executor.memory", "2g")
//      .setIfMissing("spark.eventLog.dir", "hdfs://ns1/spark/history")

    val sc = SparkContext.getOrCreate(sparkConf)

    val sqlContext = new HiveContext(sc)

    val url = "jdbc:mysql://hivemysql:3306/test"
    val table = "tb_dept"
    val props = new Properties()
    props.put("user", "root")
    props.put("password", "root")
    props.put("driver", "com.mysql.jdbc.Driver")

    sqlContext
      .read
      .table("default.dept")
      .write
      .mode(SaveMode.Overwrite)
      .jdbc(url, table, props)

    val df = sqlContext
      .read
//      .jdbc(url, table, "deptno", 10, 40, 4, props)
      .jdbc(url, table, props)

    df.registerTempTable("tmp_tb_dept")

    sqlContext
      .sql(
        """
          |SELECT a.*, b.dname, b.loc
          |FROM default.emp a JOIN tmp_tb_dept b ON a.deptno = b.deptno
        """.stripMargin
      ).registerTempTable("tmp_emp_join_dept_table")

    sqlContext.cacheTable("tmp_emp_join_dept_table")

    sqlContext
      .read
      .table("tmp_emp_join_dept_table")
        .repartition(1)
      .write
        .format("parquet")
      .mode(SaveMode.Overwrite)
      .save("/frank/spark/sql/hive_mysql_join")

    sqlContext
      .read
      .table("tmp_emp_join_dept_table")
      .write
      .format("parquet")
        .partitionBy("deptno")
      .mode(SaveMode.Overwrite)
      .saveAsTable("hive_emp_dept")

    sqlContext.uncacheTable("tmp_emp_join_dept_table")

//    bin/spark-submit \
//      --class com.frank.analyzer.sql.HiveJoinMySQLDataSparkSQL \
//      --master spark://spark-master1:7070 \
//      --deploy-mode cluster \
//    /exapp/loganalyzerlinux.jar

//        bin/spark-submit \
//          --class com.frank.analyzer.sql.HiveJoinMySQLDataSparkSQL \
//          --master spark://spark-master1:7070 \
//          --deploy-mode client \
//        /exapp/loganalyzerlinux.jar

  }

}
