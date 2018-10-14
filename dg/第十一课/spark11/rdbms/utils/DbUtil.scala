package spark11.rdbms.utils

import java.io.File
import java.sql.{Connection, DriverManager, PreparedStatement, SQLException}
import java.util
import com.typesafe.config.{Config, ConfigFactory}

/**
  * Created by hadoop on 下午4:10.
  */
object DbUtil {
  private var config: Config = null

  // 默认加载resources路径下的application.conf配置文件
  def getDBConnection: Connection = {
    config = ConfigFactory.load
    getDBConnection(config)
  }

  // 指定配置文件的完整路径
  def getDBConnection(configPath: String): Connection = {
    config = ConfigFactory.parseFile(new File(configPath))
    getDBConnection(config)
  }

  // 获取数据库连接
  private def getDBConnection(config: Config): Connection = {
    var dbConnection: Connection = null
    val dbDriver = config.getString("mysql-jdbc.driver")
    val dbUrl = config.getString("mysql-jdbc.url")
    val dbUser = config.getString("mysql-jdbc.user")
    val dbPass = config.getString("mysql-jdbc.password")
    try
      Class.forName(dbDriver)
    catch {
      case e: ClassNotFoundException =>
        System.out.println(e.getMessage)
    }
    try {
      dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPass)
      return dbConnection
    } catch {
      case e: SQLException =>
        System.out.println(e.getMessage)
    }
    dbConnection
  }

  def main(args: Array[String]): Unit = {
    var dbConnection: Connection = null
    var preparedStatement: PreparedStatement = null
    val sql = "select topic, partid from my_test"
    val result = new util.ArrayList[Tuple2[String, Int]]()

    try {
      dbConnection = getDBConnection
      preparedStatement = dbConnection.prepareStatement(sql)
      // execute select SQL stetement
      val rs = preparedStatement.executeQuery()
      while (rs.next()) {
        result.add((rs.getString(1), rs.getInt(2)))
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
    val listIterator = result.listIterator()
    while (listIterator.hasNext) {
      println(listIterator.next())
    }
  }
}
