package com.cfx.learningproject.thread.synobject.demo3;

public class ThreadA extends Thread {
    private ObjectService objectService;

    public ThreadA(ObjectService objectService) {
        super();
        this.objectService = objectService;
    }
    @Override
    public void run() {
        objectService.setUserNamePassWord("a", "aa");
    }
}
