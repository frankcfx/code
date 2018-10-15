package com.cfx.learningproject.thread.synobject.demo4;

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

    //使用synchronized(任意自定义对象)进行同步操作，
    // 对象监视器必须是同一个对象。不过不是同一个，运行就是异步执行了。
}
