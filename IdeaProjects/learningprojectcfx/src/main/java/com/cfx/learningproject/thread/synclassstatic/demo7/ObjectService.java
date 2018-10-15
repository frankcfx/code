package com.cfx.learningproject.thread.synclassstatic.demo7;


//synchronized(*.class)代码块
public class ObjectService {
    public void methodA(){
        try {
            synchronized (ObjectService.class) {
                System.out.println("methodA begin 线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
                Thread.sleep(3000);
                System.out.println("methodA end   线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void methodB(){
        synchronized (ObjectService.class) {
            System.out.println("methodB begin 线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
            System.out.println("methodB end   线程名称:"+Thread.currentThread().getName()+" times:"+System.currentTimeMillis());
        }
    }

    public synchronized void methodAC(){
        System.out.println("run----this method C");
    }

    public  void methodAD(){

//        synchronized {
//            System.out.println("run----this method D");
//        }

        //if (hiveContext == null) {
        //        synchronized {  //没有指定锁
        //          if (hiveContext == null) {
        //            hiveContext = new HiveContext(sc)
        //            generateMockData(sc, hiveContext)
        //          }
        //        }
        //      }

    }
}
