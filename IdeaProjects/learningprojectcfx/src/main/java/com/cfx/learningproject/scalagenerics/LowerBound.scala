package com.cfx.learningproject.scalagenerics

class Pair1[T](val first:T,val second:T){
  //下界通过[R >: T]的意思是
  //泛型R的类型必须是T的超类
  def replaceFirst[R >: T](newFirst:R)= {
    new Pair1[R](newFirst,second)
  }
  override def toString()=first+"---"+second
}

//class Pair2[R, T <: R  ](val first:T,val second:T){
//  def replaceFirst(newFirst:R)= {
//    new Pair1[R](newFirst,second)
//  }
//  override def toString()=first+"---"+second
//}


//Book类
class Book(val name:String){
  override def toString()="name--"+name
}
//Book子类Ebook
class Ebook(name:String) extends Book(name)
//Book子类Pbook
class Pbook(name:String) extends Book(name)
//Pbook子类,WeridBook
class WeirdBook(name:String) extends Pbook(name)

object LowerBound extends App{

  val first: Ebook = new Ebook("hello")
  val second: Pbook = new Pbook("paper book")
  val newFirst: Book = new Book("generic pBook")
  val weirdFirst:WeirdBook= new WeirdBook("generic pBook2")


  //first, second 的Class 不同， 但是 参数定义的val first:T,val second:T是相同的T
  // 所以 此时T应该是first和second 的共同基类类Book, 基类作为函数形参，
  // 当然可以传递基类的子类作为实参， 111. 此时确定范型类Pair1 中的 T是Book类型的
  val p1: Pair1[Book] = new Pair1(first,second)


//  val p12: Pair2[Nothing, Book] = new Pair2(first, second)
//
//  val p122  = p12.replaceFirst(newFirst)
//
//  val p123  = p12.replaceFirst(weirdFirst)




//  val p12= new Pair2(first,second)

//  println(p1)
  //scala> val p1 = new Pair1(first,second)
  //p1: Pair1[Book] = name--hello---name--paper book
  //Ebook,Pbook，最终得到的类是Pair1[Book]



  val p2: Pair1[Book] = p1.replaceFirst(newFirst)


//  val p22  = p12.replaceFirst(newFirst)

  //p2: Pair1[Book] = name--generic pBook---name--paper book

//  println(p2)
//  println(p22)




  //222.  此时对象p1中的T还是Book类型，但是replaceFirst 是单独定义的范型函数，
  // 而范型函数的定义是[R >: T]，其中T与范型类Pair1中的T没有关系，
  // 所以此时范型函数中的 R 与 T 还未确定类型
  val p3: Pair1[Book] = p1.replaceFirst(weirdFirst)


  //p3: Pair1[Book] = name--generic pBook---name--paper book
//  println(p3)


  val p4 = new Pair1(second,second)


  //p4: Pair1[Pbook] = name--paper book---name--paper book


//  println(p4)
//  val thirdBook=new Book("Super Books")
//  val p5=p4.replaceFirst(thirdBook)
//  println(p5)


  val p6: Pair1[Pbook] =p4.replaceFirst(second)

  val p7: Pair1[Book] =p4.replaceFirst(first)

  //下面这条语句会报错
  //type mismatch; found : cn.scala.xtwy.lowerbound.Pair1[cn.scala.xtwy.lowerbound.Pbook] required: cn.scala.xtwy.lowerbound.Pbook

  val p8: Pair1[Pbook] =p4.replaceFirst(weirdFirst)

}