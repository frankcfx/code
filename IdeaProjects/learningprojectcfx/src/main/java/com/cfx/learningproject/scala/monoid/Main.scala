package com.cfx.learningproject.scala.monoid

object Main {
  def main(args: Array[String]): Unit = {

    // 对2个元素结合
    val r = IntSemiGroup.append(1, 2)
    implicit val intMonoid = new Monoid[Int] {
      def append(a: Int, b: Int) = a + b
      def zero = 0
    }

    implicit val strMonoid = new Monoid[String] {
      def append(a: String, b: String) = a + b
      def zero = ""
    }


    println(r)

    println(List("A","B","C").foldLeft("")(_+_))

    println(List("A","B","C").foldLeft(StringMonoid.zero)(StringMonoid.append))

    val accval: String = acc(List("A","B","C","D"), StringMonoid)
    val accval2: String = acc2(List("A","B","C","D","E"))
    val accval3: String = acc3(List("A","B","C","D","E","D"))

    println(accval)
    println(accval2)
    println(accval3)

  }

  def listMonoid[T] = {
    new Monoid[List[T]] {
      def zero = Nil
      def append(a: List[T], b: List[T]) = a ++ b
    }
  }

  def acc[T](list: List[T], m: Monoid[T]): T = {
    list.foldLeft(m.zero)(m.append)
  }

  def acc2[T](list: List[T])(implicit m: Monoid[T]) = {
    list.foldLeft(m.zero)(m.append)
  }
  def acc3[T: Monoid](list: List[T]) = {
    val m = implicitly[Monoid[T]]
    list.foldLeft(m.zero)(m.append)
  }


}
