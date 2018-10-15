package com.cfx.learningproject.thread.synclassstatic.demo6;


//静态同步synchronized方法
public class ObjectService {
    public synchronized static void methodA(){
        try {
            System.out.println("static methodA begin 线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
            Thread.sleep(3000);
            System.out.println("static methodA end   线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void methodB(){
        System.out.println("static methodB begin 线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
        System.out.println("static methodB end   线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
    }
}