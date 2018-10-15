package log.analyzer.ibeifeng.com

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object GroupSortedTopN {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("SparkCoreTopN")
      //设置Master_IP
      .setMaster("spark://beifeng:7070")
      //提交的jar包在你本机上的位置
      .setJars(List("D:\\Users\\frank\\IdeaProjects\\loganalyzer\\target\\log-analyzer-1.1-SNAPSHOT.jar"))
      //设置driver端的ip,这里是你本机的ip
      .setIfMissing("spark.eventLog.enabled", "true")
      .setIfMissing("spark.executor.memory", "2g")
      .setIfMissing("spark.eventLog.dir", "hdfs://ns1/spark/history")

    //init context
    //    val sc = new SparkContext(sparkConf)
    val sc = SparkContext.getOrCreate(sparkConf)

    val path = "/user/root/data/groupsort2.txt"
    val rdd = sc.textFile(path, 3)

    val mappedRDD = rdd
      .map(line => line.split(" "))
      .filter(arr => arr.length == 2)
      .map(
        arr => {
          val word = arr(0).trim
          val value = arr(1).trim.toInt
          (word, value)
        }
      )

    val mappedRDDTopN = mappedRDD
      .groupByKey()
      .map(
        t => {
          val word = t._1
          val sortedValues = t._2.toList.sortBy( e => -e)
          println(s"1:${word}:${sortedValues.size}")
          val topNValues = sortedValues.take(3)
          (t._1, topNValues)
        }
      )


    val TopN2 = mappedRDD
        .mapPartitions( iter => {
          val random = Random
          iter.map( t => ((random.nextInt(10), t._1), t._2))
        })
      .groupByKey()
      .flatMap(
        t => {
          val word = t._1._2
          val values = t._2
          val sortedValues = values.toList.sortBy( e => -e)
          println(s"3:${word}:${sortedValues.size}")
          val topNValues = sortedValues.take(3)
          topNValues.map(value => (word,value))
        }
      )
      .groupByKey()
      .map(
        t => {
          val word = t._1
          val values = t._2
          val sortedValues = values.toList.sortBy( e => -e)
          println(s"2:${word}:${sortedValues.size}")
          val topNValues = sortedValues.take(3)
          (word, topNValues)
        }
      )

    val result3 = mappedRDD.aggregateByKey(ArrayBuffer[Int]())(
      seqOp = (buf, value) => {
        buf += value
        buf.sorted.takeRight(3)
      },
      combOp = (buf1, buf2) => {
        buf1 ++= buf2
        buf1.sorted.takeRight(3)
      }
    )
      .map( t => (t._1, t._2.toList.reverse ))

    result3.foreachPartition(iter => {
      iter.foreach( record => {
        println(record)
      })
    })

//    val result = mappedRDDTopN.collect()
//    result.foreach( record => {
//      println(record)
//    })

//    mappedRDDTopN.foreachPartition(iter => {
//      iter.foreach( record => {
//        println(record)
//      })
//    })

//    val result2 = TopN2.collect()
//    result2.foreach( record => {
//      println(record)
//    })

//    TopN2.foreachPartition(iter => {
//      iter.foreach( record => {
//        println(record)
//      })
//    })

    sc.stop()
  }
}
