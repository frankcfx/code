package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/11.
  */
object MethodDemo {
  // 最原始的函数定义方式
  def max(x: Int, y: Int): Int = {
    if (x > y) {
      println(s"grant x:${x}")
      x
    } else {
      println(s"grant y:${y}")
      y
    }
  }

  /**
    * 如果一个函数没有输入参数，但是在定义的时候给定了一个空的输入参数列表，那么调用的时候可以给定小括号也可以选择不给定, eg: f1和f1()等价
    * TODO: 当调用函数会更改对象的属性的时候，建议加小括号
    */
  def f1(): Unit = {
    println("invoke f1")
  }

  /**
    * 当一个函数没有输入参数的时候，那么输入参数列表可以省略，调用的时候就必须不能给定小括号，直接函数名称进行调用
    * TODO: 当函数的作用是修改属性的时候，最好给定参数列表
    */
  def f2: Unit = {
    println("invoke f2")
  }

  // 在定义函数的时候，可以将函数的返回值数据类型省略；但是注意当返回值类型不是Unit的时候，等于符号不能省略
  // 当不给定等于符号的时候，表示函数的返回值为Unit
  def max2(x: Int, y: Int) = {
    if (x > y) {
      x
    } else {
      y
    }
  }

  def max3(x: Int, y: Int) {
    if (x > y) {
      x
    } else {
      y
    }
  }

  // 局部函数：出现在函数内部的函数
  def f(): Unit = {
    g()
    println("invoke f()")
    def g(): Unit = {
      // g函数的作用域/可以被访问的代码区域是函数f内的所有代码均可以访问
      println("invo g()")
    }
    g()
  }

  // 默认参数函数
  // 在函数的调用过程中，可以考虑不给定输入参数的值，直接使用定义时候给定的默认值
  // NOTE: 在函数的调用过程中，采用从左往后匹配的方式；只有当没有匹配到输入值的时候，才会使用默认的参数值
  // NOTE: 要求在定义默认参数的时候，最好将默认参数放到尾部(最右侧)
  // NOTE: 函数调用过程中，可以明确给定输入参数的值,而且参数的顺序没有要求, eg: max(y=12, x=24)
  def say(name: String, sayStr: String = "Hi"): Unit = {
    println(s"${sayStr}, ${name}")
  }

  // 变长函数
  // 支持定义可以接受不定长度参数列表的函数，但是要求一个函数只能有一个变长输入参数，并且位于参数列表的最右侧
  def printStr(str: String, strs: String*): Unit = {
    // 把strs当做集合来使用，而且strs一定不等于null
    println(s"str=${str}")
    println(s"strs=${strs.mkString(",")}")
  }

  def main(args: Array[String]): Unit = {
    printStr("beifeng", "gerry", "xiaoming", "spark")
    val array = Array("gerry", "xiaoming", "spark")
    // 在函数调用过程中，不能把array当做一个整体进行传输；必须转换为将集合中的元素当做一个一个的整体进行传输
    // 使用下划线：在变长参数函数的调用过程中，下划线的作用是指函数的调用不是传递整个集合做为一个整体传输，而且集合中的每个元素进行传递
    printStr("beifeng", array: _*)
  }

}
