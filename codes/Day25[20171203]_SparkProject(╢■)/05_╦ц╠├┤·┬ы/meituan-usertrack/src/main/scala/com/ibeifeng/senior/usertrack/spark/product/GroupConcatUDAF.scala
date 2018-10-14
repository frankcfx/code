package com.ibeifeng.senior.usertrack.spark.product

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types.{DataType, StringType, StructField, StructType}

/**
  * Created by ibf on 12/03.
  */
object GroupConcatUDAF extends UserDefinedAggregateFunction {
  override def inputSchema: StructType = {
    StructType(Array(
      StructField("v1", StringType)
    ))
  }

  override def bufferSchema: StructType = {
    StructType(Array(
      StructField("b1", StringType)
    ))
  }

  override def dataType: DataType = StringType

  override def deterministic: Boolean = true

  override def initialize(buffer: MutableAggregationBuffer): Unit = buffer.update(0, "")

  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    // 基于输入数据input更新buffer中的值
    // 1. 获取输入数据, 格式为: "101:上海"
    val inputValues = input.getString(0)

    // 2. 获取缓冲区中的值，格式为: "101:上海:1,102:杭州:1,103:苏州:1"或者为""
    val bufferValues = buffer.getString(0)

    // 3. 合并输入值和缓冲区的值
    val mergedValue = this.mergeValue(bufferValues, s"${inputValues}:1")

    // 4. 更新缓冲区的值
    buffer.update(0, mergedValue)
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    // 当多个缓冲区对象需要进行合并的时候调用merge方法，一般在shuffle之后用于多个分区数据临时结果数据的合并
    // 1. 获取缓冲区的值, 格式："101:上海:1,102:杭州:1,103:苏州:1"或者为""
    val bufferValue1 = buffer1.getString(0)
    val bufferValue2 = buffer2.getString(0)

    // 2. 合并值
    var mergedValue = bufferValue1
    for (tmpStr <- bufferValue2.split(",")) {
      // tmpStr的格式为: "101:上海:1"
      // 合并mergeValue和tmpStr的值
      mergedValue = this.mergeValue(mergedValue, tmpStr)
    }

    // 3. 更新缓冲区的值
    buffer1.update(0, mergedValue)
  }

  override def evaluate(buffer: Row): Any = buffer.getString(0)

  /**
    * 合并buffer和str的值
    *
    * @param buffer 格式为: "101:上海:1,102:杭州:1,103:苏州:1"或者为""
    * @param str    格式为: "101:上海:1"
    * @return
    */
  private def mergeValue(buffer: String, str: String): String = {
    if (str == null || str.isEmpty) {
      buffer
    } else if (buffer == null || buffer.isEmpty) {
      str
    } else {
      val arr = str.split(":")
      val key = s"${arr(0)}:${arr(1)}"
      if (buffer.contains(key)) {
        buffer
          .split(",")
          .map(v => {
            if (v.contains(key)) {
              // 需要增加value的值
              val value = v.split(":")(2).toInt + arr(2).toInt
              s"${key}:${value}"
            } else {
              v
            }
          })
          .mkString(",")
      } else {
        s"${buffer},${str}"
      }
    }
  }
}
