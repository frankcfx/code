package com.cfx.learningproject.thread.synthis.demo1;

public class MainTest {
    public static void main(String[] args) {
        ObjectService service=new ObjectService();

        ThreadA a=new ThreadA(service);
        a.setName("a");
        a.start();

        ThreadB b=new ThreadB(service);
        b.setName("b");
        b.start();
    }

    //结论：
    //
    //当一个线程访问ObjectService的一个synchronized (this)同步代码块时，
    // 其它线程对同一个ObjectService中其它的synchronized (this)同步代码块的访问将是堵塞，
    // 这说明synchronized (this)使用的对象监视器是一个。
}
