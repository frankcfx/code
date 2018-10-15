package com.kafka

import kafka.utils.ZkUtils

object ZkKafkaTest {

  def main(args: Array[String]): Unit = {

    //zookeeper1:2181,zookeeper2:2181,zookeeper3:2181/kafka07
    val zkUrl = "zookeeper1:2181,zookeeper2:2181,zookeeper3:2181/kafka07"
    //    val zkClientAndConn: (ZkClient, ZkConnection) = ZkUtils.createZkClientAndConnection(zkUrl,sessTimeout,connTimeOut)
    val zkClientAndConn = ZkUtils.createZkClientAndConnection(zkUrl,30000,30000)
    val zkUtils = new ZkUtils(zkClientAndConn._1, zkClientAndConn._2, false)
    val zKPartitions = zkUtils.getPartitionsForTopics(Seq("mykafka1")).get("mykafka1").toList.head.size
    val thead = zkUtils.getPartitionsForTopics(Seq("mykafka1")).get("mykafka1").toList.head
    val theadd = zkUtils.getPartitionsForTopics(Seq("mykafka1")).get("mykafka1")

    println(zKPartitions)
    println(thead)
    println(theadd)
  }

}
