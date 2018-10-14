package com.ibeifeng.senior.scala

/**
  * Created by ibf on 11/12.
  */
class Queue {

  class Q1[T]

  class Q2[+T]

  class Q3[-T]

  class A

  class B extends A

  object Demo {
    var a: A = new A()
    var b: B = new B()
    a = b
    // 成功的
    //    b = a // error


    var qa1: Q1[A] = new Q1[A]()
    var qb1: Q1[B] = new Q1[B]()
    //    qa1 = qb1 // error
    //    qb1 = qa1// error

    var qa2: Q2[A] = new Q2[A]()
    var qb2: Q2[B] = new Q2[B]()
    qa2 = qb2
    // 成功的
    //    qb2 = qa2 // error

    var qa3: Q3[A] = new Q3[A]()
    var qb3: Q3[B] = new Q3[B]()
    //    qa3 = qb3 // error
    qb3 = qa3 // 成功的
  }

}
