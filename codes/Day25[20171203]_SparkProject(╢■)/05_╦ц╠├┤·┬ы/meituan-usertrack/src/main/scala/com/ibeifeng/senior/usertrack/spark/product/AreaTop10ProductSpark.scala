package com.ibeifeng.senior.usertrack.spark.product

import java.util.Properties

import com.alibaba.fastjson.{JSON, JSONObject}
import com.ibeifeng.senior.usertrack.conf.ConfigurationManager
import com.ibeifeng.senior.usertrack.constant.Constants
import com.ibeifeng.senior.usertrack.dao.factory.DAOFactory
import com.ibeifeng.senior.usertrack.domain.Task
import com.ibeifeng.senior.usertrack.mock.MockDataUtils
import com.ibeifeng.senior.usertrack.spark.util.{SQLContextUtil, SparkConfUtil, SparkContextUtil}
import com.ibeifeng.senior.usertrack.util.ParamUtils
import org.apache.spark.sql.{DataFrame, SQLContext}

/**
  * Created by ibf on 12/03.
  */
object AreaTop10ProductSpark {
  lazy val url = ConfigurationManager.getProperty(Constants.JDBC_URL)
  lazy val username = ConfigurationManager.getProperty(Constants.JDBC_USER)
  lazy val password = ConfigurationManager.getProperty(Constants.JDBC_PASSWORD)
  lazy val properties = {
    val prop = new Properties()
    prop.put("user", username)
    prop.put("password", password)
    prop
  }

  def main(args: Array[String]): Unit = {
    // 一、根据传入的任务id获取任务参数
    // 1.1 获取传入的参数: 任务id
    val taskId = ParamUtils.getTaskIdFromArgs(args)
    // 1.2 根据taskid获取对应的task任务对象(从RDBMs中获取数据)
    /**
      * 读取RDBMs数据的方式：
      * -1. 通过原生的JDBC API来读取数据
      * -2. 通过第三方的jar文件来读取数据, eg: mybatis、ibatis....
      * -3. 可以通过SparkSQL读取数据
      */
    /**
      * NOTE：由于在Spark应用中，读取数据一般不需要考虑链接的关闭(链接的关闭一定是在数据输出完成/读入完成后)，所以一般不需要什么数据库连接池，所以一般很少使用第三方的框架
      */
    val task: Task = if (taskId == null) {
      throw new IllegalArgumentException(s"参数异常，taskID为空:${taskId}；输入参数列表为:${args.mkString("[", ",", "]")}")
    } else {
      // a. 获取JDBC的连接对象
      val taskDao = DAOFactory.getTaskDAO
      // b. 根据taskid获取task对象
      taskDao.findByTaskId(taskId)
    }
    // 1.3 获取task的过滤参数
    if (task == null) {
      throw new IllegalArgumentException(s"参数异常, 没法在数据库中找到对应taskID的任务:${taskId}")
    }
    val taskParam: JSONObject = ParamUtils.getTaskParam(task)
    if (taskParam == null || taskParam.isEmpty) {
      throw new IllegalArgumentException(s"任务的过滤参数为空，taskID:${taskId}")
    }

    // 二、上下文对象创建
    // 0. 读取任务参数
    val isLocal = ConfigurationManager.getBoolean(Constants.SPARK_LOCAL)
    val appName = Constants.SPARK_APP_NAME_PRODUCT + taskId
    // 2.1 构建SparkConf
    val conf = SparkConfUtil.generateSparkConf(appName = appName, isLocal = isLocal)
    // 2.2 构建SparkContext
    val sc = SparkContextUtil.getSparkContext(conf)
    // 2.3 构建SQLContext对象
    /**
      * 实际运行的时候，数据一定是存储在Hive中，所以构建的一定是HiveContext对象；但是在开发测试过程中，由于访问Hive比较麻烦，一般采用的是模拟数据的测试方式，这种情况下，一般使用SQLContext对象，并且读取文本数据注册成为临时表
      */
    val sqlContext = SQLContextUtil.getInstance(
      sc,
      integratedHive = true, // 因为使用了row_number这个窗口分析函数，所以必须使用HiveContext对象作为程序入口；但是这里没有不需要访问hive表(本地执行的时候), 所以不需要考虑hive的集成
      generateMockData = (sc, sqlContext) => {
        // 产生模拟数据，并将模拟数据注册成为临时表
        if (isLocal) {
          // 加载用户行为数据和user_info数据
          MockDataUtils.mockData(sc, sqlContext)
          // 加载商品信息数据
          MockDataUtils.loadProductInfoMockData(sc, sqlContext)
        }
      }
    )

    // UDF
    def isEmpty(str: String) = str == null || str.isEmpty || "null".equalsIgnoreCase(str)
    sqlContext.udf.register("isEmpty", (str: String) => isEmpty(str))
    sqlContext.udf.register("isNotEmpty", (str: String) => !isEmpty(str))
    sqlContext.udf.register("concat_int_string", (id: Int, name: String) => s"${id}:${name}")
    sqlContext.udf.register("fetch_product_type", (json: String) => {
      if (isEmpty(json)) {
        "未知"
      } else {
        val obj = JSON.parseObject(json)
        val t = obj.getString("product_type")
        t match {
          case "0" => "自营商品"
          case "1" => "第三方商品"
          case _ => s"未知:${t}"
        }
      }
    })
    sqlContext.udf.register("fetch_area_level", (area: String) => {
      // fetch_area_level(area) as area_level
      area match {
        case "华东" | "华南" | "华北" => "A"
        case "华中" => "B"
        case "西南" | "东北" => "C"
        case "西北" => "D"
        case _ => "E"
      }
    })
    // UDAF
    sqlContext.udf.register("group_contact", GroupConcatUDAF)


    // 三、数据获取及过滤(用户行为数据获取) ==> 数据是存在在Hive表中
    val actionDataFrame = this.getActionDataFrameByFilter(sqlContext, taskParam)

    // 四、城市信息数据读取形成DataFrame
    val cityInfoDataFrame = this.getCityInfoDataFrame(sqlContext)

    // 五、合并用户行为数据和城市信息表数据，得到区域和商品id的合并数据，合并结果保存临时表
    this.generateTempProductBasicTable(sqlContext, actionDataFrame, cityInfoDataFrame)

    // 六、统计各个区域下面的各个商品的次数
    this.generateTempAreaProductClickCountTable(sqlContext)

    // 七、统计各个区域访问次数最多的前10个商品
    this.generateTempAreaTop10ProductClickCountTable(sqlContext)

    // 八、对数据进行一个信息的补全（商品数据补全）
    this.generateTempAreaTop10FullProductClickCountTable(sqlContext)

    // 九、持久化数据，将结果输出到关系数据库中
    this.persistAreaTop10FullProductClickCount(sqlContext)

  }

  /**
    * 将最终结果输出到关系数据库中
    *
    * @param sqlContext
    */
  def persistAreaTop10FullProductClickCount(sqlContext: SQLContext): Unit = {
    /**
      * DataFrame的数据输出到RDBMs中的方式
      * 一、通过DataFrame的write的jdbcAPI进行数据输出
      * 没法实现记录级别的Insert-or-Update
      * 二、通过DataFrame/RDD的foreachPartition API自定义具体的数据输出代码
      */
    // 1. 读取数据形成DataFrame
    val df = sqlContext.table("tmp_area_top10_full_product_count")
    // 2. 结果输出
    df.show(20)
  }

  /**
    * 补全商品信息数据，并将结果保存为临时表
    *
    * @param sqlContext
    */
  def generateTempAreaTop10FullProductClickCountTable(sqlContext: SQLContext): Unit = {
    /**
      * 获取json格式的数据，可以使用if语句块+自定义函数的方式来获取
      * if(condition, true-value, false-value)
      * 如果condition这个条件为真的话，返回true-value的内容，如果为假，返回false-value的值
      */
    /**
      * case-when
      * area: 华南、华北、华中、华东、东北、西南、西北
      * 级别的划分：
      * 华南、华北、华东 => A
      * 华中 => B
      * 东北、西南 => C
      * 西北 => D
      * 其它 => E
      */
    // 1. hql语句编写
    val hql =
    """
      | SELECT
      |   CASE
      |     WHEN tatpcc.area = '华南' OR tatpcc.area = '华东' OR tatpcc.area = '华北' THEN 'A'
      |     WHEN tatpcc.area = '华中' THEN 'B'
      |     WHEN tatpcc.area = '西南' OR tatpcc.area = '东北' THEN 'C'
      |     WHEN tatpcc.area = '西北' THEN 'D'
      |     ELSE 'E'
      |   END AS area_level,
      |   tatpcc.area,
      |   tatpcc.click_product_id AS product_id,
      |   tatpcc.click_count,
      |   tatpcc.city_infos,
      |   pi.product_name,
      |   if (isEmpty(pi.extend_info), '未知', fetch_product_type(pi.extend_info)) AS product_type
      | FROM
      |   tmp_area_top10_product_click_count tatpcc
      |     LEFT JOIN product_info pi ON tatpcc.click_product_id = pi.product_id
    """.stripMargin

    // 2. hql语句执行
    val df = sqlContext.sql(hql)

    // 3. 临时表注册
    df.registerTempTable("tmp_area_top10_full_product_count")
  }

  /**
    * 基于各个区域各个商品的点击次数获取各个区域中点击次数最多的前10个商品，最终结果保存临时表
    *
    * ===> 分组排序TopN的程序 ==> row_number
    * @param sqlContext
    */
  def generateTempAreaTop10ProductClickCountTable(sqlContext: SQLContext): Unit = {
    // 1. hql语句的编写
    val hql1 =
    """
      | SELECT
      |   area, click_product_id, click_count, city_infos,
      |   ROW_NUMBER() OVER (PARTITION BY area ORDER BY click_count DESC) AS rnk
      | FROM tmp_area_product_click_count
    """.stripMargin

    // 2. hql语句的执行
    val df1 = sqlContext.sql(hql1)

    // 3. 注册临时表
    df1.registerTempTable("tmp_area_product_click_count_rnk")

    // 4. hql语句的编写&执行&注册临时表
    sqlContext
      .sql(
        """
          | SELECT area, click_product_id, click_count, city_infos
          | FROM tmp_area_product_click_count_rnk
          | WHERE rnk <= 10
        """.stripMargin)
      .registerTempTable("tmp_area_top10_product_click_count")
  }

  /**
    * 计算各个区域各个商品的点击次数，并将结果注册临时表
    * 一条数据就算一个点击记录，不涉及到去重
    *
    * @param sqlContext
    */
  def generateTempAreaProductClickCountTable(sqlContext: SQLContext): Unit = {
    /**
      * 原始数据:
      * eg:
      * 101 上海 华东 1
      * 102 杭州 华东 1
      * 103 苏州 华东 1
      * 201 北京 华北 1
      * 202 天津 华北 1
      * 201 北京 华北 1
      * 201 北京 华北 1
      *
      * 期望结果：
      * 华东 1 3 101:上海:1,102:杭州:1,103:苏州:1
      * 华北 1 4 201:北京:3,202:天津:1
      *
      * 1. 对一条数据的不同列的值进行操作 ==> UDF函数 ==> 一条数据输入一条数据输出
      * --- 对一条数据的city_id和city_name进行合并连接操作
      * 2. 对一组数据的同列值进行操作 ==> UDAF函数 ==> 一组数据输入，一条数据输出
      * --- 对city_id和city_name合并之后的值进行同组数据的聚合操作
      */
    // 1. hql语句编写
    val hql =
    """
      | SELECT
      |   area, click_product_id, COUNT(1) as click_count,
      |   group_contact(concat_int_string(city_id, city_name)) AS city_infos
      | FROM tmp_product_basic
      | GROUP BY area, click_product_id
    """.stripMargin

    // 2. hql语句执行
    val df = sqlContext.sql(hql)

    // 3. DataFrame注册临时表
    df.registerTempTable("tmp_area_product_click_count")
  }

  /**
    * 合并(join)action和cityInfo两个DataFrame，并将结果注册临时表
    *
    * @param sqlContext
    * @param action
    * @param cityInfo
    */
  def generateTempProductBasicTable(sqlContext: SQLContext, action: DataFrame, cityInfo: DataFrame): Unit = {
    action
      .join(cityInfo, "city_id") // city_id要求在两个DataFrame中均存在，默认join为inner join
      .select("city_id", "city_name", "area", "click_product_id") // 获取列
      .registerTempTable("tmp_product_basic")

    // hql实现
    /*action.registerTempTable("tmp_action")
    cityInfo.registerTempTable("tmp_city_info")
    sqlContext
      .sql(
        """
          | SELECT
          |   a.city_id, b.city_name, b.area, a.click_product_id
          | FROM tmp_action a
          |   JOIN tmp_city_info b
          |     ON a.city_id = b.city_id
        """.stripMargin)
      .registerTempTable("tmp_product_basic")*/
  }

  /**
    * 从RDBMs中读取数据形成DataFrame
    *
    * @param sqlContext
    * @return
    */
  def getCityInfoDataFrame(sqlContext: SQLContext): DataFrame = {
    // NOTE: 注意一下SQLContext的read.jdbc三种API的区别，以及选择一个适合的API
    val table = "city_info"
    sqlContext
      .read
      .jdbc(url, table, properties)
  }

  /**
    * 根据过滤参数获取过滤数据
    *
    * @param sqlContext
    * @param taskParam
    * @return
    */
  def getActionDataFrameByFilter(sqlContext: SQLContext, taskParam: JSONObject): DataFrame = {
    // 1. 获取过滤参数
    val startDate: Option[String] = ParamUtils.getParam(taskParam, Constants.PARAM_START_DATE)
    val endDate = ParamUtils.getParam(taskParam, Constants.PARAM_END_DATE)
    val sex = ParamUtils.getParam(taskParam, Constants.PARAM_SEX)
    // 职业支持多选，数据使用逗号进行分割，eg: 程序员,教师,设计师
    val professionals = ParamUtils.getParam(taskParam, Constants.PARAM_PROFESSIONALS)
    val professionalWhereStr = professionals
      .map(v => {
        val pro = v
          .split(",")
          .map(t => s"'${t}'")
          .mkString("(", ",", ")")
        s" AND ui.professional IN ${pro} "
      })
      .getOrElse("")

    // 获取是否需要join user表
    val needJoinUserInfoTable = if (sex.isDefined || professionals.isDefined) Some(true) else None

    // 2. 编写HQL语句
    /*val hql =
    s"""
       | SELECT
       |   uva.click_product_id, uva.city_id
       | FROM user_visit_action uva
       |   ${needJoinUserInfoTable.map(v => " JOIN user_info ui ON uva.user_id=ui.user_id ").getOrElse("")}
       | WHERE isNotEmpty(uva.click_product_id)
       |   ${startDate.map(v => s" AND uva.date >= '${v}' ").getOrElse("")}
       |   ${endDate.map(v => s" AND uva.date <= '${v}' ").getOrElse("")}
       |   ${sex.map(v => s" AND ui.sex = '${v}' ").getOrElse("")}
       |   ${professionalWhereStr}
    """.stripMargin*/
    val hql =
    s"""
       | SELECT
       |   uva.pay_product_ids AS click_product_id, uva.city_id
       | FROM user_visit_action uva
       |   ${needJoinUserInfoTable.map(v => " JOIN user_info ui ON uva.user_id=ui.user_id ").getOrElse("")}
       | WHERE isNotEmpty(uva.pay_product_ids)
       |   ${startDate.map(v => s" AND uva.date >= '${v}' ").getOrElse("")}
       |   ${endDate.map(v => s" AND uva.date <= '${v}' ").getOrElse("")}
       |   ${sex.map(v => s" AND ui.sex = '${v}' ").getOrElse("")}
       |   ${professionalWhereStr}
    """.stripMargin
    println(s"==========\n${hql}\n===========")

    // 3. HQL语句的执行
    val df = sqlContext.sql(hql)
    // 4. 返回最终的结果数据
    import sqlContext.implicits._
    df
      .flatMap(row => {
        val pids = row.getAs[String]("click_product_id")
        val cid = row.getAs[Int]("city_id")
        pids
          .split(Constants.SPLIT_CATEGORY_OR_PRODUCT_ID_SEPARATOR_ESCAOE)
          .map(pid => (pid, cid))
      })
      .toDF("click_product_id", "city_id")
  }
}
