package com.cfx.learningproject.rpcdemo.api;

public interface HelloService {
    String sayHello(String name);
    Person getPerson(String name);
}
