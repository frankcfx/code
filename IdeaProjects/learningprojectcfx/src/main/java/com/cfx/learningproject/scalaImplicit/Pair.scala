package com.cfx.learningproject.scalaImplicit

class Pair[T: Ordering](val first: T, second:T) {

  def smaller(implicit ord:Ordering[T]) =
    if (ord.compare(first, second) < 0) first else second

  def smaller2 = if (implicitly[Ordering[T]].compare(first, second) < 0) first else second
}

object PairTest {
  def main(args: Array[String]): Unit = {

    val p1 = new Pair(20, 4)

    //    Ordering.Int
    println(p1.smaller(Ordering.Int))

//    println(p1.smaller2)

    Ordering.Int

//    println(Ordering.Int)

  }
}



