package com.cfx.learningproject.thread.synclassstatic.demo6;

public class ThreadA extends Thread {

    @Override
    public void run() {
        ObjectService.methodA();
    }
}
