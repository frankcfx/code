package spark11.rdbms

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by zhoucw on 下午10:25.
  */
object RddCase {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("local[*]").setAppName("RddCase")
    val sc = new SparkContext(sparkConf)

    val list = List(1,2,3,4)
    val rddPara = sc.parallelize(list)
    val rddMake = sc.makeRDD(list)

    rddPara.collect().foreach(r=>println("parallelize:" + r))
    rddMake.collect().foreach(r=>println("rddMake:" + r))

    val listPrefer = List((1,List("spark123", "spark1234")),
      (2,List("spark001", "spark002")),
      (3,List("spark003")),
      (4,List("spark004")))

    val rddPrefer = sc.makeRDD(listPrefer)


    /*
    * 分配本地Scala集合以形成RDD，并为每个对象提供一个或多个位置首选项（Spark节点的主机名）。
    * 为每个集合项目创建一个新的分区。
    * */
    /*##############################################
      rddPrefer:1
      rddPrefer:2
      rddPrefer:3
      rddPrefer:4
     *##############################################
     */
    rddPrefer.collect().foreach(r=>println("rddPrefer:" + r))
    println(rddPrefer.preferredLocations(rddPrefer.partitions(0))) // List(spark123, spark1234)
    println(rddPrefer.preferredLocations(rddPrefer.partitions(1))) // List(spark001, spark002)


    /*##############################################
     (1,List(spark123, spark1234))
    (2,List(spark001, spark002))
    (3,List(spark003))
    (4,List(spark004))
    *##############################################
    */
    sc.parallelize(listPrefer).collect().foreach(r=>println("rddPrefer:" + r))
  }
}
