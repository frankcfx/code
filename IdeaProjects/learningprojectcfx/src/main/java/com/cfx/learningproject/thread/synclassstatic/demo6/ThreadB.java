package com.cfx.learningproject.thread.synclassstatic.demo6;

public class ThreadB extends Thread {
    @Override
    public void run() {
        ObjectService.methodB();
    }
}
