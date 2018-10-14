package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/12.
  */
object WeekdayEnum extends Enumeration{
  type WeekdayEnum = Value
  // 开始定义枚举对象
  val FIRST, SECOND = Value
  val FOUR = Value(10)
  val FIX = Value
  val SIX = Value("abc")
}
