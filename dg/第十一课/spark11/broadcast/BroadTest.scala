package spark11.broadcast

import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by hadoop on 上午1:40.
  */
object BroadTest {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setMaster("local[2]").setAppName("test")
    val sc = new SparkContext(conf)


    //商品信息(商品id,商品名称)
    val goods_info = sc.parallelize(Array(("001","phone"),("002","tv"),("003","notebook"))).collectAsMap()
    //订单详细信息(商品id, 订单地址, 订单id)
    val orders = sc.parallelize(Array(("001","hefei","1000023"),
      ("002","beijing","1000024"),
      ("003","nanjing","1000025"),
      ("002","hangzhou","1000026")))

    // 广播小的数据集
    val goodinfoBroad = sc.broadcast(goods_info)


    val res2 = orders.mapPartitions(iter =>{
      val goodMap = goodinfoBroad.value
      val arrayBuffer = ArrayBuffer[(String,String,String)]()
      iter.foreach{case (goodsId,address,orderId) =>{
        if(goodMap.contains(goodsId)){
          arrayBuffer.+= ((orderId, goodMap.getOrElse(goodsId,""),address))
        }
      }}
      arrayBuffer.iterator
    })

/*    val res = orders.map(iter =>{
      val goodMap = goodinfoBroad.value
      val arrayBuffer = ArrayBuffer[(String,String,String)]()
      if(goodMap.contains(iter._1)){
        arrayBuffer.+= ((iter._1, goodMap.getOrElse(iter._1,""),iter._2))
      }
      arrayBuffer
    })*/



    /**
      * 使用yield实现
      * */
    val res1 = orders.mapPartitions(iter => {
      val goodMap = goodinfoBroad.value
      for{
        (goodId, address, orderid) <- iter
        if(goodMap.contains(goodId))
      } yield (goodId, goodMap.getOrElse(goodId,""),address)
    })

    res2.foreach(println)
    res1.foreach(println)
  }
}
