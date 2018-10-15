package com.cfx.learningproject.thread.synobject.demo3;

public class MainTest {
    public static void main(String[] args) {
        ObjectService service=new ObjectService();
        ThreadA a=new ThreadA(service);
        a.setName("A");
        a.start();
        ThreadB b=new ThreadB(service);
        b.setName("B");
        b.start();
    }

    //多个线程持有对象监视器作为同一个对象的前提下，
    //同一时间只有一个线程可以执行synchronized(任意自定义对象)同步代码快。
}
