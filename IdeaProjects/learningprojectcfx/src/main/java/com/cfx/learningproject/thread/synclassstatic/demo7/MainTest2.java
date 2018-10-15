package com.cfx.learningproject.thread.synclassstatic.demo7;

public class MainTest2 {
    public static void main(String[] args) {

        //分别实例化一个对象
        ObjectService service1=new ObjectService();
        ObjectService service2=new ObjectService();
        ObjectService service3=new ObjectService();

        ThreadA a=new ThreadA(service1);
        a.setName("A");
        a.start();

        ThreadB b=new ThreadB(service2);
        b.setName("B");
        b.start();

        ThreadC c=new ThreadC(service3);
        c.setName("c");
        c.start();
    }

    //同步synchronized(*.class)代码块的作用其实和
    // synchronized static方法作用一样。Class锁对类的所有对象实例起作用。
}