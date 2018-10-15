package com.frank.analyzer.log

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer
import scala.util.Random

object GroupSortedTopN2 {

  def main(args: Array[String]): Unit = {
    // 一、创建上下文
    val conf = new SparkConf()
      .setAppName("GroupSortedTopN2")
      //设置Master_IP
      .setMaster("local")
//      .setMaster("spark://spark-master1:7070")
      //提交的jar包在你本机上的位置
//      .setJars(List("/home/cfx/IdeaProjects/loganalyzerlinux/target/log-analyzer-1.0-SNAPSHOT.jar"))
      //设置driver端的ip,这里是你本机的ip
      .setIfMissing("spark.eventLog.enabled", "true")
//      .setIfMissing("spark.executor.memory", "2g")
      .setIfMissing("spark.eventLog.dir", "hdfs://ns1/spark/history")

    // NOTE: 因为SparkContext表示的是一个spark应用的上下文，而且构建SparkContext对象的地方就是Driver进程，又说一个Spark应用只能有一个Driver进程，所以说Spark默认要求一个JVM中SparkContext对象只允许存在一个
    // 一般不建议使用new的方式来构建SparkContext对象
    //    val sc = new SparkContext(conf)
    val sc = SparkContext.getOrCreate(conf)

    // 二、RDD的操作
    // 2.1 读取数据形成RDD
    // 给定文件所处的HDFS路径，当给定的路径中没有给定schema的时候，使用默认的文件系统(即fs.defaultFS配置的文件系统)；
    // 也就是说当没有集成hdfs的时候，默认文件系统为local
    val path = "/user/root/data/groupsort2.txt"
    val rdd = sc.textFile(path, 5)

    // 2.2 数据的预处理
    val mappedRDD: RDD[(String, Int)] = rdd
      .map(line => line.split(" "))
      .filter(arr => arr.length == 2)
      .map(arr => (arr(0).trim, arr(1).trim.toInt))
    mappedRDD.cache()

    // 2.3 实现方式一：基于groupByKey + map
    /**
      * 思路：先按照key进行分组(groupByKey), 然后对分组之后的数据(key为单词，value为所有单词相同的数字组成的一个迭代器)进行处理，
      * 处理方式为：对迭代器中的数据进行排序操作；最后获取排序好的迭代器中的前K个值
      * NOTE: 一般情况下，要尽可能的减少RDD的调用链(如果调用链太多，就有可能导致task没法运行<task的序列化后的大小是有限制的，
      * 如果超过限制，那么driver没法将task发送给executor的>) ===> 这种问题的解决方案很简单：1. 合并API(合并map、flatMap、filter之类的API);
      * 2. 在rdd中进行cache缓存或者checkpoint
      * 缺点1：groupByKey这个API在当前版本中，同组(相同)key对应的value数据会形成一个迭代器，并且这个迭代器在后续的处理中会全部的加载到内存中；
      * 所以如果一个key对应的value数据太多，就有可能出现数据倾斜或者OOM异常
      * 缺点2：在同组key数据进行聚合的业务场景中，groupByKey API的性能有点低，因为不会进行分区数据的局部聚合(不会降低shuffle的数据量)，
      * 而groupByKey API会将所有的数据都发送给下一个阶段(会进行shuffle操作)，在聚合的业务场景中，
      * shuffle数据中其实有部分数据是无用的(不需要考虑) ==> 实际的聚合业务场景中，一般一个分区中只有部分数据需要考虑
      */
    /*mappedRDD
      .groupByKey()
      .map(t => {
        val word: String = t._1
        val values: Iterable[Int] = t._2
        // 排序: 因为默认的sorted api的排序是升序，所以这里将其按照相反数(乘以-1之后的数字)排序，那么最终结果就是降序
        val sortedValue = values.toList.sortBy(t => -t)
        (word, sortedValue)
      })
      .map(t => {
        val word = t._1
        val values = t._2
        val topNValues = values.take(3)
        (word, topNValues)
      })*/
    val result1 = mappedRDD
      .groupByKey()
      .map(t => {
        val word: String = t._1
        // NOTE：此时这个values这个迭代器的数据全部位于内存中
        val values: Iterable[Int] = t._2
        // 排序: 因为默认的sorted api的排序是升序，所以这里将其按照相反数(乘以-1之后的数字)排序，那么最终结果就是降序
        val sortedValue = values.toList.sortBy(t => -t)
//        println(s"1：${word}:${sortedValue.size}")
        // 获取topN
        val topNValues = sortedValue.take(3)
        // 最终结果返回
        (word, topNValues)
      })
    // 输出结果
    result1.foreachPartition(iter => {
//      iter.foreach(println)
    })
    /*result1
      .flatMap(t => {
        t._2.map(tt => (t._1, tt))
      })
      .foreachPartition(iter => iter.foreach(println))*/

    // 2.4 实现方式二：两阶段聚合(groupByKey+flatMap+groupByKey+map)
    // driver中定义了一个random的变量，在executor中使用了该变量，所以要求random变量是可以进行序列化操作的(因为需要网络传输)；
    // 但是这里的这个random变量不能序列化操作，所以不能传递，
    // 如果直接运行会报错: Exception in thread "main" org.apache.spark.SparkException: Task not serializable
    //    val random = Random
    val result2 = mappedRDD
      .mapPartitions(iter => {
        val random = Random
        iter.map(t => ((random.nextInt(100), t._1), t._2))
      })
      .groupByKey()
      .flatMap(t => {
        // 局部数据的topN获取
        val word: String = t._1._2
        val values: Iterable[Int] = t._2
        val sortedValue = values.toList.sortBy(t => -t)
//        println(s"3：${word}:${sortedValue.size}")
        val topNValues = sortedValue.take(3)
        topNValues.map(value => (word, value))
      })
      .groupByKey()
      .map(t => {
        // 全局数据的topN获取
        val word: String = t._1
        val values: Iterable[Int] = t._2
        val sortedValue = values.toList.sortBy(t => -t)
//        println(s"2：${word}:${sortedValue.size}")
        val topNValues = sortedValue.take(3)
        (word, topNValues)
      })
    // 输出结果
    result2.foreachPartition(iter => {
//      iter.foreach(println)
    })

    // 2.5 实现方式三：aggregateByKey ==> 解决groupByKey API的两个缺点
    /**
      *  数据聚合，根据给定的函数对集合中的元素进行数据聚合操作
    // 先聚合1和2的值，得到一个临时聚合值；然后临时聚合值和3的值聚合，再得到一个临时的聚合值；以此迭代聚合4、5、6、7....的值
      * def aggregateByKey[U: ClassTag](zeroValue: U)(seqOp: (U, V) => U,
      * combOp: (U, U) => U): RDD[(K, U)]
      * U：每组key对应的最终的聚合值类型
      * V：每组key对应的原始value数据的类型
      * zeroValue: 初始值，每组key对应的一个初始值，即最初的聚合值
      * seqOp: 对每组key对应的value数据进行迭代处理，将value和之前的聚合值(U值)进行聚合操作，
      * 并最终返回一个新的聚合U值 ==> 在shuffle之前，对当前分区中对应key的value进行聚合操作的时候会被调用
      * combOp：对两个分区的聚合数据进行一个全局聚合操作，并返回一个新的聚合值 ==> 在shuffle发送数据之后，
      * 对多个分区的结果值进行聚合操作的时候会被调用；该API在数据量比较大，并且内存不够的情况下，
      * 有可能会在shuffle之前触发(首先将seqOp操作的结果输出磁盘，然后对磁盘的输出结果调用combOp进行合并操作 ---> 类似MapReduce中的combiner操作)
      */
    val result3 = mappedRDD.aggregateByKey(ArrayBuffer[Int]())(
      seqOp = (buf, value) => {
        // 将value添加到buf中，然后获取最大的前3个值
        println("aggregate1")
        buf += value
        buf.sorted.takeRight(3)
      },
      combOp = (buf1, buf2) => {
        // 合并两个buffer中的值，然后获取最大的前3个值
        println(s"aggregate2:${buf1.mkString("[",",","]")}:${buf2.mkString("[",",","]")}")
        buf1 ++= buf2
        buf1.sorted.takeRight(3)
      }
    )
      .map(t => {
        (t._1, t._2.toList.reverse)
      })
    // 输出结果
    result3.foreachPartition(iter => {
      iter.foreach(println)
    })

    /**
      * WordCount代码
      *1. mappedRDD.reduceByKey((v1,v2) => v1 + v2)
      *2. mappedRDD.aggregateByKey(0)(
      * seqOp = (u, v) => u + v,
      * combOp = (u1, u2) => u1 + u2
      * )
      */
    // 清除缓存 ==> 这里其实不需要清除，原因是：当应用结束的时候，会自动的清除缓存
    mappedRDD.unpersist()
  }

}
