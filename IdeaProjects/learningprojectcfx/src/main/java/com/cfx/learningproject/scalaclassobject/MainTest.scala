package com.cfx.learningproject.scalaclassobject

class Student(val name:String){

//  this(name:String) = {
//    this.name = name
//  }
  def staticobject = Student.studentNo

//  object ddd = ""
}

object Student extends Ordering[String]{
  private var studentNo:Int=0;
  def uniqueStudentNo()={
    studentNo+=1
    studentNo
  }

  override def compare(x: String, y: String): Int = 0

//  def getClassVal = this.name
}
object MainTest {

  def main(args: Array[String]): Unit = {
    val s1 = new Student("nam")

    testPrint(s1)
    testPrint2(Student)

  }

  def testPrint(cc: Student)={
    println(cc.name)
  }

  def testPrint2(cc: Ordering[String])={
    println("test")
  }

}
