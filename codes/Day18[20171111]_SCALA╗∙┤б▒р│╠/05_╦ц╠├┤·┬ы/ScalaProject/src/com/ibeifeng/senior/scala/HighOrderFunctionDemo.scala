package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/11.
  */
object HighOrderFunctionDemo {
  /**
    * greeting就是高阶函数
    *
    * @param name string类型的一个输入参数
    * @param func 函数类型的一个输入参数，并且这个函数要求是String输入，Unit返回的一个函数对象
    */
  def greeting(name: String, func: (String) => Unit): Unit = {
    println("invoke greeting")
    func(name)
  }

  def sayHi(name: String): Unit = {
    println(s"Hi,${name}")
  }

  def sayHello(name: String): Unit = {
    println(s"hello,${name}")
  }

  def oper(a: Int, b: Int, op: (Int, Int) => Int): Unit = {
    println(s"result=${op(a, b)}")
  }


  def main(args: Array[String]): Unit = {
    val name = "gerry"
    val sayHiFunc = sayHi _
    greeting(name, sayHiFunc)
    val sayHelloFunc = sayHello _
    greeting("xiaoming", sayHelloFunc)
    greeting("xiaoliu", sayHello _)
    greeting("xiaoliu", sayHello) // 可以基于上下文进行推断，判断出这里的sayHello其实需要的是函数对象，而不是函数的执行结果，所以不需要给定下划线

    // 匿名函数和高阶函数一起使用，匿名函数作为高阶函数的参数传递进来
    greeting("gerry", (name: String) => {
      println(s"nihao,${name}")
      println("abc")
    })
    greeting("gerry", (name: String) => println(s"nihao,${name}"))
    oper(10, 12, (a: Int, b: Int) => a + b)

    // 高阶函数的简化
    // 1. 可以将输入参数的类型省略，原因是：scala的编译器会自动的根据函数定义的上下文推断此处的数据输入类型是啥
    greeting("gerry", (name) => println(s"nihao,${name}"))
    oper(10, 12, (a, b) => a + b)
    // 2. 当输入参数只有一个的时候，可以省略小括号
    greeting("gerry", name => println(s"nihao,${name}"))
    oper(10, 12, (a, b) => a + b)
    // 3. 如果左侧的所有输入参数在右侧的函数体中均被使用，而且满足一下条件:
    // a. 参数有且仅使用一次
    // b. 参数的使用顺序和输入参数的顺序一致
    // 当满足上述两个条件的时候，就可以省略输入参数，使用下划线代替(备注：省略之后没有异议，可以自动推断出类型)
    //    greeting("gerry", println(s"nihao,${_}")) // 报错，原因在于：没法进行format格式化
    oper(10, 12, _ + _) // 第一个下划线表示函数的第一个输入参数，第二个下划线表示函数的第二个输入参数


  }
}
