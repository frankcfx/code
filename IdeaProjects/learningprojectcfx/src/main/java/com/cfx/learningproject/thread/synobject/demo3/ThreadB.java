package com.cfx.learningproject.thread.synobject.demo3;

public class ThreadB extends Thread {
    private ObjectService objectService;

    public ThreadB(ObjectService objectService) {
        super();
        this.objectService = objectService;
    }
    @Override
    public void run() {
        objectService.setUserNamePassWord("b", "bb");
    }
}
