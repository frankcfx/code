package com.frank.analyzer.sql

import org.apache.spark.sql.Row
import org.apache.spark.sql.expressions.{MutableAggregationBuffer, UserDefinedAggregateFunction}
import org.apache.spark.sql.types._

/**
  * avg = total sum / total count = 总和 / 总数
  * 注意：在处理的过程中需要保留临时的总和和总数的值(sum和count的值)
  * Created by ibf on 11/25.
  */
object SelfAVGUDAF extends UserDefinedAggregateFunction{
  override def inputSchema: StructType = {
    // 给定输入数据的数据类型
    StructType(Array(
      StructField("v1", DoubleType)
    ))
  }

  override def bufferSchema: StructType = {
    // 给定执行过程中的缓存保留值数据类型
    StructType(Array(
      StructField("b1", DoubleType),
      StructField("b2", IntegerType)
    ))
  }

  override def dataType: DataType = {
    // 最终返回值数据类型
    DoubleType
  }

  override def deterministic: Boolean = true

  override def initialize(buffer: MutableAggregationBuffer): Unit = {
    // 初始化缓存值
    buffer.update(0, 0.0)
    buffer.update(1, 0)
  }

  override def update(buffer: MutableAggregationBuffer, input: Row): Unit = {
    // update这个api在处理每一条数据的时候被调用，主要功能是：将每一条输入数据和缓冲区中的数据进行合并操作
    // ===> 将input的数据合并到buffer中
    // 1. 读取输入数据
    val inputValue = input.getDouble(0)

    // 2. 读取缓冲区中的值
    val bufferSum = buffer.getDouble(0)
    val bufferCount = buffer.getInt(1)

    // 3. 更新缓冲区的值
    buffer.update(0, bufferSum + inputValue)
    buffer.update(1, bufferCount + 1)
  }

  override def merge(buffer1: MutableAggregationBuffer, buffer2: Row): Unit = {
    // NOTE: merge这个API主要在多个分区结果合并的时候会调用，但是也有可能会在shuffle之前的combiner中被调用
    // 合并两个分区的直接结果, 也就是合并两个缓冲区的结果
    // 1. 获取缓冲区的结果数据
    val bufferSum1 = buffer1.getDouble(0)
    val bufferCount1 = buffer1.getInt(1)
    val bufferSum2 = buffer2.getDouble(0)
    val bufferCount2 = buffer2.getInt(1)

    // 2. 更新缓冲区
    buffer1.update(0, bufferSum1 + bufferSum2)
    buffer1.update(1, bufferCount1 + bufferCount2)
  }

  override def evaluate(buffer: Row): Any = {
    // 计算最终结果并返回
    buffer.getDouble(0) / buffer.getInt(1)
  }
}
