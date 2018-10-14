package spark88.utils

import java.util.Base64

import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{StructField, _}


/**
  * 解析日志
  */
object ParseUtils {
  val struct = StructType(Array(
    StructField("houseid", IntegerType),
    StructField("srcip", StringType),
    StructField("destip", StringType),
    StructField("proctype", StringType),
    StructField("srcport", StringType),

    StructField("destport", StringType),
    StructField("domain", StringType),
    StructField("url", StringType),
    StructField("duration", StringType),
    StructField("acctime", StringType),

    StructField("dayid", StringType),
    StructField("hourid", StringType)
  ))

  /**
    * from unix time to dayid/hourid
    *
    * @param time
    * @return tuple3(time, dayid, hourid)
    */
  def getTime(time: String): Tuple3[String, String, String] = {
    try {
      val targetfdf = FastDateFormat.getInstance("yyyyMMddHHmmss")
      val timeStr = targetfdf.format(time.toLong * 1000)
      val dayid = timeStr.substring(0, 8).replaceAll("-", "")
      val hourid = timeStr.substring(8, 10)
      (timeStr, dayid, hourid)
    } catch {
      case e: Exception => {
        ("0", "0", "0")
      }
    }
  }


  /**
    *
    * @param line
    * @return Row
    */
  def getRow(line: Tuple4[Long, String, Int, Object]) = {
    val offset  = line._1
    val topic = line._2
    val partition = line._3
    val msg = line._4.toString
    try {
      val arr = msg.split("\\|", 10)
      var houseid = 0
      val timeTuple = getTime(arr(9))
      try {
        houseid = arr(0).toInt
      } catch {
        case e: Exception => {
        }
      }

      var url = arr(7)
      try {
        val urlBytes = Base64.getDecoder.decode(arr(7))
        url = new String(urlBytes, "utf-8")
      } catch {
        case e: Exception => {
        }
      }
      Row(houseid, arr(1), arr(2), arr(3), arr(4), arr(5), arr(6), url, arr(8), timeTuple._1, timeTuple._2, timeTuple._3)
    } catch {
      case e: Exception => {
        val houseid = -1
        Row(houseid, line._4, "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1", "-1")
      }
    }
  }

  def main(args: Array[String]): Unit = {
    println(getTime("1521172762"))

    val asBytes = Base64.getDecoder.decode("aHR0cDovL2J4ZnNmcy5zeGwubWUvdGVtcC9iM2Y2NDM1YmQzNjc4MzRhMzUwNGE5YzYxMDdmMmRjYS5waHA/bT0x")

    System.out.println(new String(asBytes, "utf-8"))
  }
}
