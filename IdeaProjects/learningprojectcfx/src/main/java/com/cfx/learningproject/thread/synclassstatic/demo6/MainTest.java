package com.cfx.learningproject.thread.synclassstatic.demo6;


public class MainTest {
    public static void main(String[] args) {
        ThreadA a=new ThreadA();
        a.setName("A");
        a.start();
        ThreadB b=new ThreadB();
        b.setName("B");
        b.start();
    }

    //synchronized应用在static方法上，那是对当前对应的*.Class进行持锁。
}