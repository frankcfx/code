package com.cfx.learningproject.atomic;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * https://blog.csdn.net/lovincc/article/details/75208290
 * https://blog.csdn.net/yzzst/article/details/46469331
 */
public class Aomication2 implements Runnable{
    private static int i = 0;
    private static AtomicBoolean bool = new AtomicBoolean(false);
    private String name;
    Aomication2(String name){
        this.name = name;
    }
    @Override
    public  void run() {
        //Atomic就是原子性的意思，即能够保证在高并发的情况下只有一个线程能够访问这个属性值
        if (bool.compareAndSet(false,true)) {
            ++i;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("小明 是个:  " + name);
            System.out.println("小明 2222:  " + name);
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("菜菜:  " + name);
            System.out.println("崔崔    :  " + name);

            System.out.println(i);
            bool.set(false);
        }else{
            System.out.println("线程失败" + name);
        }
    }

    public static void main(String[] args) {
        Thread thead1 = new Thread(new Aomication2("逗笔"));
        Thread thead2 = new Thread(new Aomication2("傻笔"));

        thead1.start();
        thead2.start();
    }

}
