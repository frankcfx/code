package com.cfx.learningproject.thread.synthis.demo2;

public class ObjectService {


    //synchronized thread name:Thread-0-->i=1
    //synchronized thread name:Thread-0-->i=2
    //synchronized thread name:Thread-0-->i=3
    //run----objectMethodA  可以看到objectMethodA方法异步执行了，下面我们将objectMethodA()加上同步。
    //synchronized thread name:Thread-0-->i=4
    //synchronized thread name:Thread-0-->i=
//    public void objectMethodA(){
//        System.out.println("run----objectMethodA");
//    }

    public synchronized void objectMethodA(){
        System.out.println("run----objectMethodA");
    }

    public void objectMethodB(){
        synchronized (this) {
            try {
                for (int i = 1; i <= 10; i++) {
                    System.out.println("synchronized thread name:"+Thread.currentThread().getName()+"-->i="+i);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
