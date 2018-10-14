package com.ibeifeng.senior.usertrack.spark.ad

import java.util.Properties

import com.ibeifeng.senior.usertrack.conf.ConfigurationManager
import com.ibeifeng.senior.usertrack.constant.Constants
import com.ibeifeng.senior.usertrack.spark.util.{SQLContextUtil, SparkConfUtil, SparkContextUtil}
import com.ibeifeng.senior.usertrack.util.DateUtils
import kafka.serializer.StringDecoder
import org.apache.spark.HashPartitioner
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SaveMode
import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.dstream.{DStream, SelfReducedWindowedDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

import scala.util.{Failure, Success, Try}

/**
  * Created by ibf on 12/03.
  */
object AdClickRealTimeStateSpark {
  // 参数获取
  lazy val isLocal = ConfigurationManager.getBoolean(Constants.SPARK_LOCAL)

  lazy val url = ConfigurationManager.getProperty(Constants.JDBC_URL)
  lazy val table = "tb_black_users"
  lazy val props = {
    val prop = new Properties()
    val username = ConfigurationManager.getProperty(Constants.JDBC_USER)
    val password = ConfigurationManager.getProperty(Constants.JDBC_PASSWORD)
    prop.put("user", username)
    prop.put("password", password)
    prop
  }

  def main(args: Array[String]): Unit = {
    // 一、上下文的构建
    // 1. 获取相关变量
    val appName = Constants.SPARK_APP_NAME_AD
    // 2. 构建上下文对象
    val conf = SparkConfUtil.generateSparkConf(appName, isLocal)
    // 3. SparkContext获取
    val sc = SparkContextUtil.getSparkContext(conf)
    // 4. StreamingContext的构建
    // NOTE: 一般在工作中，SparkStreaming程序的运行方式为HA的方式；这里的代码编写就不使用HA的方式
    // batchDuration: 指定默认的批次产生间隔时间，一般要求：该值比批次的平均执行时间要大(大5%~10%)
    val ssc = new StreamingContext(sc, Seconds(10))
    // 5. 给定checkpoint文件夹路径
    ssc.checkpoint(s"/beifeng/spark/streaming/ha${System.currentTimeMillis()}")

    // 二、SparkStreaming和Kafka的集成  ==> 实际工作中，一般是采用direct模式，这里使用Receiver的方式
    val kafkaParams = Map(
      "zookeeper.connect" -> ConfigurationManager.getProperty(Constants.KAFKA_ZOOKEEPER_URL),
      "group.id" -> appName
    )
    val topics = ConfigurationManager.getProperty(Constants.KAFKA_AD_TOPICS)
      .split(",")
      .map(v => v.split(":"))
      .filter(arr => arr.length == 2)
      .map(arr => (arr(0), arr(1).trim.toInt)) // 指定的是消费那个topic以及消费该topic的时候产生的KafkaStream是几个
      .toMap
    val dstream = KafkaUtils.createStream[String, String, StringDecoder, StringDecoder](
      ssc, // 上下文
      kafkaParams, // kafka连接的相关consumer的参数
      topics, // 指定进行消费的topic名称以及使用多少个线程进行消费
      StorageLevel.MEMORY_AND_DISK_2 // 指定block的缓存级别
    ).map(_._2)

    // 三、数据的格式化转换
    val formattedDStream = this.formattedAdRealTimeDStreamData(dstream)

    // 四、黑名单数据过滤
    val filteredDStream = this.filterByBlackList(formattedDStream)

    // 五、黑名单数据更新(动态更新机制)
    this.dynamicUpdateBlackList(filteredDStream)

    // 六、实时累加统计广告的点击量
    /**
      * 实时统计每天、每个省份、每个城市、每个广告的点击量
      * 数据不涉及到去重操作
      */
    val aggrDStream = this.calculateRealTimeState(filteredDStream)

    // 七、获取各个省份每天的Top5的广告点击数据
    this.calculateProvinceTop5Ad(aggrDStream)

    // 八、获取最近一段时间的广告点击趋势
    this.calculateAdClickCountByWindow(filteredDStream)

    // 九、启动
    ssc.start()
    ssc.awaitTermination()
  }

  /**
    * 目的：为了查看广告的点击趋势
    * 实时统计最近十分钟的广告点击量
    * 使用window函数进行分析
    * -1. 窗口大小：指定新生成的DStream的每个批次执行的时候，处理的数据量是最近多久的数据的
    * window interval: 60 * 10 = 600s
    * -2. 滑动大小：指定新生成的DStream每隔多久产生一个待运行的批次
    * slider interval: 60 * 1 = 60s
    * NOTE: window interval还是slider interval均要求是父DStream的批次产生时间的整数倍
    *
    * @param dstream
    */
  def calculateAdClickCountByWindow(dstream: DStream[AdClickRecord]): Unit = {
    // 1. 数据转换
    val mappedDStream = dstream.map(record => (record.adID, 1))

    // 2. 窗口分析
    // NOTE: 使用window类型优化的API的时候，必须给定checkpoint文件夹路径
    val aggDStream: DStream[(Int, Int)] = mappedDStream
      .reduceByKeyAndWindow(
        (a: Int, b: Int) => a + b, // 给定一个聚合函数
        (c: Int, d: Int) => c - d, // c表示上一个批次的执行结果，d表示上一个批次和当前批次没有重叠部分的聚合值
        Minutes(10), // 指定窗口大小
        Minutes(1) // 指定滑动大小
      )

    /**
      * 直白来讲，进行展示的时候是展示广告随着时间变化，它的这个点击量的变化情况
      * 所以要在最终结果中添加一个表示时间的字段属性 ==> 最好的选择方案是：批次产生时间
      */
    // 3. 添加批次产生时间
    val finalDStream: DStream[(Int, String, Int)] = aggDStream.transform((rdd, time) => {
      // time即批次产生时间
      val dateStr = DateUtils.parseLong2String(time.milliseconds, "yyyyMMddHHmmss")
      rdd.map(t => (t._1, dateStr, t._2))
    })

    // 4. 结果输出
    finalDStream.print(5)
  }

  /**
    * 获取各个省份每天的Top5的广告点击数据
    * 1. 将一个省份不同城市的广告点击数据进行聚合操作
    * 2. 在获取各个省份的top5的值 => 分组排序TopN
    *
    * @param dstream
    */
  def calculateProvinceTop5Ad(dstream: DStream[((String, String, String, Int), Long)]): Unit = {
    // 1. 聚合各个省份的广告点击量
    val dailyAndClickCountDStream = dstream
      .map {
        case ((date, province, _, adID), count) => ((date, province, adID), count)
      }
      .reduceByKey(_ + _)

    // 2. 获取各个省份各个日期点击次数最多的前5个广告
    val top5ProvinceAdClickCountDStream = dailyAndClickCountDStream.transform(rdd => {
      rdd
        .map {
          case ((date, province, adID), count) => {
            ((date, province), (adID, count))
          }
        }
        .groupByKey()
        .flatMap {
          case ((date, province), iter) => {
            // 从iter中获取出现次数最多的前5个值
            val top5Iter = iter
              .toList
              .sortBy(t => t._2)
              .takeRight(5)
            top5Iter
              .map {
                case (adId, count) => ((date, province, adId), count)
              }
          }
        }
    })

    // 3. 数据保存数据库
    // NOTE: 作业自己完善
    top5ProvinceAdClickCountDStream.print(5)
  }

  /**
    * 实时统计每天、每个省份、每个城市、每个广告的点击量
    * 数据不涉及到去重操作
    * ===> 以日期、省份、城市以及广告作为key，以点击量作为value
    *
    *
    * @param dstream
    * @return
    */
  def calculateRealTimeState(dstream: DStream[AdClickRecord]): DStream[((String, String, String, Int), Long)] = {
    // 1. 将DStream转换为key/value的键值对
    val mappedDStream: DStream[((String, String, String, Int), Int)] = dstream
      .map {
        case AdClickRecord(timestamp, province, city, _, adID) => {
          // 将timestamp转换为日期字符串，格式为: yyyyMMdd
          val date = DateUtils.parseLong2String(timestamp, DateUtils.DATEKEY_FORMAT)
          ((date, province, city, adID), 1)
        }
      }

    // 2. 累加统计数值
    // NOTE: 使用updateStateByKey的时候，必须给定checkpoint文件夹路径
    /**
      * def updateStateByKey[S: ClassTag](
      * updateFunc: (Iterator[(K, Seq[V], Option[S])]) => Iterator[(K, S)],
      * partitioner: Partitioner,
      * rememberPartitioner: Boolean
      * ): DStream[(K, S)]
      */
    val partitioner = new HashPartitioner(dstream.context.sparkContext.defaultParallelism)
    val rememberPartitioner = true
    val newUpdateFunc = (iterator: Iterator[((String, String, String, Int), Seq[Int], Option[Long])]) => {
      iterator.flatMap(t => {
        // 0. 获取key中的date值
        val date = t._1._1
        // 1. 获取当前的时间date值
        val currentDate = DateUtils.parseLong2String(System.currentTimeMillis(), DateUtils.DATEKEY_FORMAT)

        if (date.equalsIgnoreCase(currentDate)) {
          // 进行数据的更新操作
          // 2. 获取当前key对应的传递过来的value的值
          val currentValue = t._2.sum
          // 3. 获取之前状态保存值
          val preValue = t._3.getOrElse(0L)
          // 4. 更新状态值并返回
          Some(preValue + currentValue)
            .map(s => (t._1, s))
        } else {
          // 否则表示不需要保留状态值
          None
        }
      })
    }
    /*mappedDStream
      .reduceByKey(_ + _)
      .updateStateByKey(
        (values: Seq[Int], state: Option[Long]) => {
          // 1. 获取当前key对应的传递过来的value的值
          val currentValue = values.sum

          // 2. 获取之前状态保存值
          val preValue = state.getOrElse(0L)

          // 3. 更新状态值并返回
          // NOTE: 需要删除上一天的累加统计值，也就是说，如果累加值是上一天的，那么进行删除操作 ==> 返回None
          Some(preValue + currentValue)
        }
      )*/
    val aggrDStream: DStream[((String, String, String, Int), Long)] = mappedDStream
      .reduceByKey(_ + _)
      .updateStateByKey(
        updateFunc = newUpdateFunc,
        partitioner,
        rememberPartitioner
      )

    // 3. 数据输出到MySQL
    // TODO: 作业自己实现
    aggrDStream.print(5)

    // 4. 结果返回
    aggrDStream
  }

  /**
    * 动态黑名单更新机制
    *
    * @param dstream
    */
  def dynamicUpdateBlackList(dstream: DStream[AdClickRecord]): Unit = {
    // 因为dstream中的数据已经是经过黑名单数据过滤的，可以认为通过dstream计算出来的黑名单用户数据在以前的批次中一定是属于正常用户的
    // 黑名单的规则：最近5分钟内广告点击次数超过100次的用户就是异常用户

    // 1. 计算一下当前批次中的用户广告点击次数（每三分钟执行一次，每次执行最近五分钟的数据）
    /*val combinerDStream = dstream
      .map(record => (record.userID, 1))
      .reduceByKeyAndWindow(
        (a: Int, b: Int) => a + b,
        Minutes(5), // 窗口大小，指定每个批次计算的时候，计算最近五分钟的数据
        //        Minutes(3) // 滑动大小，表示每隔三分钟产生一个批次
        Seconds(30) // 滑动大小，表示每个30秒产生一个批次
      )*/
    /**
      * 异常：java.sql.BatchUpdateException: Duplicate entry '7166' for key 'PRIMARY'
      * 异常产生的原因：是由于数据库中已经存在了一个id为7166的黑名单用户，然后这次又进行7166用户插入到数据库，导致主键冲突，出现的异常
      * 解决方案：将数据的输出方式(Insert)更改为Insert or Update操作即可解决这个问题
      * 为什么产生这个异常？？？？
      * 思考一下reduceByKeyAndWindow这个API的执行方式，会将多个父DStream的批次的执行集合合并成为子DStream的一个批次来执行；在DStream的执行过程中，如果存在stage(ShuffleMapStage)在之前有运行过，那么在当前的批次中，这个stage不会再运行，直接从shuffle的磁盘中获取数据；反应在DAG图中的话就是灰色的stage
      * 所以如果一个用户在上一个批次中计算为黑名单用户，同时这个用户是出现在上一个批次和当前批次重叠的这一部分的；那么在当前批次计算过程中，重叠这一部分会直接从shuffle中获取数据(获取的这个数据其实就包含上一个批次执行过程中存在的黑名单用户)，从而导致最终形成的RDD中包含的有上一个批次的黑名单用户
      * 解决方案：
      * -1. 将数据的输出方式(Insert)更改为Insert or Update操作即可解决这个问题
      * -2. 自定义DStream的window类型的API(更改API的实现方式)
      */
    /*val combinerDStream = dstream
      .map(record => (record.userID, 1))
      .reduceByKeyAndWindow(
        (a: Int, b: Int) => {
          // a表示临时聚合值，b表示同组key中对应的value值
          if (a < 0) {
            // 表示在上一个批次，当前用户就是黑名单用户了
            -1
          } else {
            a + b
          }
        },
        (c: Int, d: Int) => {
          // c表示的是上一个批次的执行结果; 当c大于100的时候，表示这个用户已经属于黑名单用户了，不需要再输出到数据库啦
          // NOTE: 因为当上一个批次和当前批次执行的时候，非重叠部分没有聚合值的情况下，是不会调用该函数的
          if (c > 100) {
            -1
          } else {
            c - d
          }
        },
        Minutes(5), // 窗口大小，指定每个批次计算的时候，计算最近五分钟的数据
        //        Minutes(3) // 滑动大小，表示每隔三分钟产生一个批次
        Seconds(30) // 滑动大小，表示每个30秒产生一个批次
      )*/
    val mappedDStream = dstream.map(record => (record.userID, 1))
    val combinerDStream = SelfReducedWindowedDStream.reduceByKeyAndWindow(
      (a: Int, b: Int) => {
        // a表示临时聚合值，b表示同组key中对应的value值
        if (a < 0) {
          // 表示在上一个批次，当前用户就是黑名单用户了
          -1
        } else {
          a + b
        }
      },
      (c: Int, d: Int) => {
        // c表示的是上一个批次的执行结果;
        // 当c的值小于0的时候，表示在上一个批次中，当前用户就是黑名单用户了, 不需要统计啦
        if (c < 0) {
          -1
        } else {
          c - d
        }
      },
      Minutes(5), // 窗口大小，指定每个批次计算的时候，计算最近五分钟的数据
      //        Minutes(3) // 滑动大小，表示每隔三分钟产生一个批次
      Seconds(30), // 滑动大小，表示每个30秒产生一个批次
      mappedDStream.context,
      mappedDStream,
      (c: Int) => {
        // c表示上一个批次的执行结果; 当c大于100的时候，表示当前用户就是黑名单用户了，不需要统计该用户的值
        if (c > 100) -1
        else c
      }
    )

    // 2. 黑名单数据获取
    val blackDStream: DStream[(Int, Int)] = combinerDStream
      .filter(t => t._2 > 100)
      .transform(rdd => {
        // 进行白名单用户列表过滤
        // 白名单列表也在数据库中，需要进行数据库的读取访问操作，这里直接使用模拟数据
        val sc = rdd.sparkContext
        val whiteListRDD = sc.parallelize(0 until 1000)
        val broadcastOfWhiteList = sc.broadcast(whiteListRDD.collect())

        // 基于广播变量的方式进行数据过滤
        rdd.filter(t => !broadcastOfWhiteList.value.contains(t._1))
      })

    // 3. 黑名单数据输出到关系型数据库
    /**
      * DStream的数据保存，而且是保存到关系型数据库中
      * 要不就转换为RDD进行输出，要不就转换为DataFrame进行数据输出
      */
    blackDStream.foreachRDD(rdd => {
      // 因为从图上来讲，这个rdd中的数据在数据库中应该一定不存在，所以说这个输出操作其实就是简单的append追加操作
      val sc = rdd.sparkContext
      val sqlContext = SQLContextUtil.getInstance(sc, integratedHive = !isLocal)
      import sqlContext.implicits._
      val df = rdd.toDF("user_id", "count")
      df.write.mode(SaveMode.Append).jdbc(url, table, props)
    })
  }

  /**
    * 根据保存在数据库中的黑名单数据进行数据过滤操作
    *
    * @param dstream
    * @return
    */
  def filterByBlackList(dstream: DStream[AdClickRecord]): DStream[AdClickRecord] = {
    dstream.transform(rdd => {
      // 1. 从RDBMs中读取黑名单数据
      val blackListRDD: RDD[(Int, Int)] = {
        val sc = rdd.sparkContext
        val sqlContext = SQLContextUtil.getInstance(sc, integratedHive = !isLocal)
        sqlContext
          .read
          .jdbc(url, table, props)
          .map(row => {
            val userId = row.getAs[Int]("user_id")
            val count = row.getAs[Int]("count")
            (userId, count)
          })
      }

      // 2. 数据过滤 ==> 基于左连接的数据过滤
      rdd
        .map(record => (record.userID, record))
        .leftOuterJoin(blackListRDD) // 左连接的结果，对于在rdd中出现，但是在blackListRDD中没有出现的数据，右RDD(blakListRDD)中的count为None
        .filter(_._2._2.isEmpty) // 表示只要出现在rdd中的数据，但是没有在blackListRDD中出现的数据
        .map(_._2._1)
    })
  }


  /**
    * 将DStream中的string类型的数据格式化成为对应的用户广告点击数据
    *
    * @param dstream
    * @return
    */
  def formattedAdRealTimeDStreamData(dstream: DStream[String]): DStream[AdClickRecord] = {
    /*dstream
      .map(line => line.split(" "))
      .filter(arr => arr.length == 5)
      .map(arr => {
        Try {
          AdClickRecord(
            arr(0).toLong,
            arr(1),
            arr(2),
            arr(3).toInt,
            arr(4).toInt
          )
        }
      })
      .filter(t => t.isSuccess)
      .map(t => t.get)
    */
    dstream
      .flatMap(line => {
        val arr = line.split(" ")

        Try {
          AdClickRecord(
            arr(0).toLong,
            arr(1),
            arr(2),
            arr(3).toInt,
            arr(4).toInt
          )
        } match {
          case Success(record) => {
            // 直接返回结果值
            Iterator.single(record).toIterable
          }
          case Failure(exception) => {
            // TODO: 这里可以进行异常处理操作
            Iterable.empty[AdClickRecord]
          }
        }
      })
    // TODO: 每调用一次DStream的API，就相当于构建一个DStream的依赖关系，那么这样最终如果调用链过长的话，就有可能导致task过大，没法从driver中传递给executor；所有有时候可以考虑将多个连续的窄依赖的API进行合并，可以减少DStream/RDD的构建次数；对于比较复杂的业务场景而言的话，有一定的性能提升
  }
}

case class AdClickRecord(
                          timestamp: Long,
                          province: String,
                          city: String,
                          userID: Int,
                          adID: Int
                        )