package com.ibeifeng.bigdata.spark.app.sql

import java.sql.DriverManager

/**
  * Created by ibf on 11/19.
  */
object SparkJDBCThriftServerDemo {
  def main(args: Array[String]): Unit = {
    // 一、添加driver
    val driver = "org.apache.hive.jdbc.HiveDriver"
    Class.forName(driver)
    // 二、获取connection连接
    val conn = DriverManager.getConnection("jdbc:hive2://hadoop-senior01.ibeifeng.com:10000", "xiaoming", "")
    // 三、sql语句的执行
    val sql = "select a.ename, a.empno, a.sal, b.dname from common.emp a join common.dept b on a.deptno=b.deptno where a.sal > ?"
    val pstmt = conn.prepareStatement(sql)
    pstmt.setInt(1, 1500)
    val rs = pstmt.executeQuery()
    while (rs.next()) {
      println(s"${rs.getInt(s"empno")}, ${rs.getString("ename")}, ${rs.getDouble("sal")}, ${rs.getString("dname")}")
    }
    // 四、关闭连接
    rs.close()
    pstmt.close()
    conn.close()
  }
}
