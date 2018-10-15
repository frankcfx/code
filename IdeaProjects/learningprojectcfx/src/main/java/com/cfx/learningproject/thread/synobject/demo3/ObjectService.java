package com.cfx.learningproject.thread.synobject.demo3;

//将任意对象作为对象监视器
public class ObjectService {
    private String uname;
    private String pwd;
    //锁定的是相同线程
//    String lock=new String();
    public void setUserNamePassWord(String userName,String passWord){
        //锁定的是不同线程
        String lock=new String();
        try {
            synchronized (lock) {
                System.out.println("thread name="+Thread.currentThread().getName()
                        +" 进入代码快:"+System.currentTimeMillis());
                uname=userName;
                Thread.sleep(3000);
                pwd=passWord;
                System.out.println("thread name="+Thread.currentThread().getName()
                        +" 进入代码快:"+System.currentTimeMillis()+"入参uname:"+uname+"入参pwd:"+pwd);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
