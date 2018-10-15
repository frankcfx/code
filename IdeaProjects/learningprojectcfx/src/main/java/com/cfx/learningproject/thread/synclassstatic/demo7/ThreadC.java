package com.cfx.learningproject.thread.synclassstatic.demo7;

public class ThreadC extends Thread {
    private ObjectService objectService;

    public ThreadC(ObjectService objectService) {
        super();
        this.objectService = objectService;
    }
    @Override
    public void run() {
        objectService.methodAC();
    }
}
