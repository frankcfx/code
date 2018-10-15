package com.jeremy.process.multithread;



class Thread4 extends Thread{
    private String name;
    public Thread4(String name) {
        this.name=name;
    }
    public void run() {
        for (int i = 0; i < 5; i++) {
            System.out.println(name + "运行  :  " + i);
        }

    }
}
public class MainPriority {
    public static void main(String[] args) {
        Thread4 t1 = new Thread4("t1");
        Thread4 t2 = new Thread4("t2");

        //java多线程对于多核cpu来说输出无先后顺序,但对于单核的cpu来说由于它们都会放在同一个队列中，这时候会有先后顺序，
        // 但是对于多核cpu来说，会出现多个队列，如果线程不在同一个队列当中那就无确定的先后顺序
        t1.setPriority(Thread.MAX_PRIORITY);
        t2.setPriority(Thread.MIN_PRIORITY);

        t1.start();
        t2.start();
    }
}
