package com.cfx.learningproject.scalavariance

//定义自己的List类
class List1[T](val head: T, val tail: List[T])

//covariance
class List[+T](val head: T, val tail: List[T]){

  //下面的方法编译会出错
  //covariant type T occurs in contravariant position in type T of value newHead
  //编译器提示协变类型T出现在逆变的位置
  //即泛型T定义为协变之后，泛型便不能直接
  //应用于成员方法当中
//  def prepend2(newHead:T):List[T]=new List(newHead,this)



  //将函数也用泛型表示
  //因为是协变的，输入的类型必须是T的超类
  def prepend[U>:T](newHead:U):List[U]=new List(newHead,this)

  override def toString()=""+head

}

object NonVariance {
  def main(args: Array[String]): Unit = {
    //编译报错
    //type mismatch; found :
    //cn.scala.xtwy.covariance.List[String] required:
    //cn.scala.xtwy.covariance.List[Any]
    //Note: String <: Any, but class List
    //is invariant in type T.
    //You may wish to define T as +T instead. (SLS 4.5)
//    val list= new List[String]("摇摆少年梦")
//    val list: List1[String] = new List1[String]("摇摆少年梦",null)
    val list:List[Any]= new List[String]("摇摆少年梦",null)
    val ttt: List[Any] = list.prepend("hello")
    println(ttt)


  }
}
