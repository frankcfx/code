package com.ibeifeng.bigdata.spark.app.sql

import java.util.Properties

import org.apache.spark.sql.SaveMode
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ibf on 11/25.
  */
object HiveJoinMySQLDataSparkSQL {
  def main(args: Array[String]): Unit = {
    // 一、上下文的构建
    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("hive-join-mysql")
    val sc = SparkContext.getOrCreate(conf)
    // 因为这里需要访问hive表，所以必须使用HiveContext对象作为程序的入口
    val sqlContext = new HiveContext(sc)

    val url = "jdbc:mysql://hadoop-senior01.ibeifeng.com:3306/test"
    val table = "tb_dept"
    val props = new Properties()
    props.put("user", "root")
    props.put("password", "123456")
    props.put("driver", "com.mysql.jdbc.Driver")

    // 1. hive表数据导入MySQL
    sqlContext
      .read
      .table("common.dept")
      .write
      .mode(SaveMode.Overwrite) // 当表存在的时候，进行表删除然后再进行重新的构建操作； DataFrame的jdbc API输出的话，不能实现记录级别的insert or update
      .jdbc(url, table, props)

    // 2. hive表和mysql表join(不同源的数据join)
    // 2.1 读取Hive表数据形成临时表
    val df = sqlContext
      .read
      .jdbc(url, table, props)
    //      .jdbc(url, table, "deptno", 10, 30, 4, props)
    //      .jdbc(url, table, Array("deptno < 15", "deptno >= 15 AND deptno < 30", "deptno >= 30"), props)
    df.registerTempTable("tmp_tb_dept")
    // 2.2 两张表数据join
    sqlContext
      .sql(
        """
          |SELECT a.*, b.dname, b.loc
          |FROM common.emp a join tmp_tb_dept b on a.deptno = b.deptno
        """.stripMargin)
      .registerTempTable("tmp_emp_join_dept_table")

    // 3. 数据输出
    // 当一张表的数据被多次使用的时候，进行缓存操作
    sqlContext.cacheTable("tmp_emp_join_dept_table")
    // 3.1 输出到HDFS的文件夹中，格式为：parquet
    sqlContext
      .read
      .table("tmp_emp_join_dept_table")
      .repartition(1)
      .write
      .format("parquet")
      .mode(SaveMode.Overwrite)
      .save("/beifeng/spark/sql/hive_mysql_join")
    // 3.2 将数据保存Hive表，并且设置parquet格式以及分区字段
    sqlContext
      .read
      .table("tmp_emp_join_dept_table")
      .write
      .format("parquet")
      .partitionBy("deptno")
      .mode(SaveMode.Overwrite)
      .saveAsTable("hive_emp_dept")

    // 如果缓存的表不需要了，那么记住进行un cache操作
    sqlContext.uncacheTable("tmp_emp_join_dept_table")

    Thread.sleep(10000000)

  }
}
