package com.ibeifeng.bigdata.spark.app.sql

import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by ibf on 06/03.
  */

case class Person2(name: String, age: Int, sex: String, salary: Int, deptNo: Int)

case class Dept(deptNo: Int, deptName: String)

object SparkSQLDSLDemo {
  def main(args: Array[String]): Unit = {
    // 1. 上下文创建
    val conf = new SparkConf()
      .setAppName("demo")
      .setMaster("local[*]")
    val sc = SparkContext.getOrCreate(conf)
    // 当使用HiveContext的时候需要给定jvm的参数：-XX:PermSize=128M -XX:MaxPermSize=256M
    val sqlContext = new HiveContext(sc)
    import sqlContext.implicits._
    import org.apache.spark.sql.functions._

    sqlContext.udf.register("sexToNum", (sex: String) => {
      sex.toUpperCase match {
        case "M" => 0
        case "F" => 1
        case _ => -1
      }
    })
    sqlContext.udf.register("self_avg", AvgUDAF)

    // 2. 直接创建模拟数据
    val rdd1 = sc.parallelize(Array(
      Person2("张三", 21, "M", 1235, 1),
      Person2("李四", 20, "F", 1235, 1),
      Person2("王五", 26, "M", 1235, 1),
      Person2("小明", 25, "F", 1225, 1),
      Person2("小花", 24, "F", 1425, 1),
      Person2("小华", 23, "M", 1215, 1),
      Person2("gerry", 22, "F", 1415, 2),
      Person2("tom", 21, "F", 1855, 2),
      Person2("lili", 20, "F", 1455, 2),
      Person2("莉莉", 18, "M", 1635, 2)
    ))
    val rdd2 = sc.parallelize(Array(
      Dept(1, "部门1"),
      Dept(2, "部门2")
    ))

    val personDataFrame = rdd1.toDF()
    val deptDataFrame = rdd2.toDF()

    // ====DSL==================================
    // cache 多次使用的DataFrame
    personDataFrame.cache()
    deptDataFrame.cache()

    // select语法
    println("----select-----")
    personDataFrame.select("name", "age", "sex").show()
    personDataFrame.select($"name", $"age", $"sex".as("sex1")).show()
    personDataFrame.select(col("name").as("name1"), col("age").as("age1"), col("sex")).show()
    personDataFrame.selectExpr("name", "age", "sex", "sexToNum(sex) as sex_num").show()


    // where/filter
    println("------where/filter-------")
    personDataFrame.where("age > 22").where("sex = 'M'").where("deptNo = 1").show()
    personDataFrame.where("age > 20 AND sex='M' AND deptNo = 1").show()
    personDataFrame.where($"age" > 20 && $"sex" === "M" && $"deptNo" === 1).show()
    personDataFrame.where($"age" > 20 && $"deptNo" === 1 && ($"sex" !== "F")).show()

    // sort
    println("-----------sort--------------")
    // 全局排序
    personDataFrame.sort("salary").select("name", "salary").show()
    personDataFrame.sort($"salary".desc).select("name", "salary", "age").show()
    personDataFrame.sort($"salary".desc, $"age".asc).select("name", "salary", "age").show()
    personDataFrame.orderBy($"salary".desc, $"age".asc).select("name", "salary", "age").show()
    personDataFrame
      .repartition(5)
      .orderBy($"salary".desc, $"age")
      .select("name", "salary", "age").show()
    // 局部排序(按照分区进行排序)
    personDataFrame
      .repartition(5)
      .sortWithinPartitions($"salary".desc, $"age".asc)
      .select("name", "salary", "age")
      .show()

    // group by
    personDataFrame
      .groupBy("sex")
      .agg(
        "salary" -> "avg",
        "salary" -> "sum"
      )
      .show()
    personDataFrame
      .groupBy("sex")
      .agg(
        avg("salary").as("avg_salary"),
        min("salary").as("min_salary"),
        count(lit(1)).as("cnt")
      )
      .show()
    personDataFrame
      .groupBy("sex")
      .agg(
        "salary" -> "self_avg"
      )
      .show()

    // limit
    personDataFrame.limit(2).show()

    // ==join===============
    println("----------join--------------------")
    personDataFrame.join(deptDataFrame).show()
    // 无法判断deptNo属于哪个DataFrame的会报错
    //    personDataFrame.join(deptDataFrame, $"deptNo" === $"deptNo")
    personDataFrame.join(deptDataFrame.toDF("col1", "deptName"), $"deptNo" === $"col1", "inner").show()
    personDataFrame.join(deptDataFrame, "deptNo").show()
    personDataFrame
      .join(deptDataFrame.toDF("deptNo", "name"), Seq("deptNo"), "left_outer")
      .toDF("no", "name", "age", "sex", "sal", "dname")
      .show()

    // ===窗口分析函数=======必须要是是使用HiveContext对象
    /** *
      * 按照deptNo分组，组内按照salary进行排序，获取每个部门前3个销售额的用户信息
      * select *
      * from
      * (select *, ROW_NUMBER() OVER (Partition by deptNo Order by salary desc) as rnk
      * from person) as tmp
      * where tmp.rnk <= 3
      */
    val w = Window.partitionBy("deptNo").orderBy($"salary".desc, $"age".asc)
    personDataFrame
      .select(
        $"name", $"age", $"deptNo", $"salary",
        row_number().over(w).as("rnk")
      )
      .where("rnk <= 3")
      .show()

    // 清除缓存
    personDataFrame.unpersist()
    personDataFrame.unpersist()
  }
}
