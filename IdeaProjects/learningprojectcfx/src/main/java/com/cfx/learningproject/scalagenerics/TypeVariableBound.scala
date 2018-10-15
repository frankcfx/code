package com.cfx.learningproject.scalagenerics

//下面的类编译通不过
//因为泛型T在编译的时候不能确定其具体类型
//即并不是所有的类中都存在compareTo方法
class TypeVariableBound {
//  def compare[T](first:T,second:T)={
//    if (first.compareTo(second)>0)
//      first
//    else
//      second
//  }

  //采用<:进行类型变量界定
  //该语法的意思是泛型T必须是实现了Comparable
  //接口的类型
  def compare[T <: Comparable[T]](first:T,second:T)={
    if (first.compareTo(second)>0)
      first
    else
      second
  }
}

object TypeVariableBound{
  def main(args: Array[String]): Unit = {
    val tvb=new TypeVariableBound
    println(tvb.compare("A", "B"))
  }
}







