package com.cfx.learningproject.thread.synobject.demo4;


//synchronized(任意自定义对象)与synchronized同步方法共用
public class ObjectService {
    private String lock=new String();
    public void methodA(){
        try {
            synchronized (lock) {
                System.out.println("a begin");
                Thread.sleep(3000);
                System.out.println("a   end");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public synchronized void methodB(){
        System.out.println("b begin");
        System.out.println("b   end");
    }
}
