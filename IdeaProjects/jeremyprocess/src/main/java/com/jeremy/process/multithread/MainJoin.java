package com.jeremy.process.multithread;

public class MainJoin {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getName()+"主线程运行开始!");
        Thread3 mTh1=new Thread3("A");
        Thread3 mTh2=new Thread3("B");
        mTh1.start();
        mTh2.start();
        try {
            mTh1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            mTh2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName()+ "主线程运行结束!");


    }
}
