package com.cfx.learningproject.thread.synclassstatic.demo7;

public class MainTest {
    public static void main(String[] args) {

        //测试方法是共同对象
        ObjectService service=new ObjectService();
        ThreadA a=new ThreadA(service);
        a.setName("A");
        a.start();
        ThreadB b=new ThreadB(service);
        b.setName("B");
        b.start();
    }
}
