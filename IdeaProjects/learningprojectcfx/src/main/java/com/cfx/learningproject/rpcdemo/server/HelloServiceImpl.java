package com.cfx.learningproject.rpcdemo.server;

import com.cfx.learningproject.rpcdemo.api.HelloService;
import com.cfx.learningproject.rpcdemo.api.Person;

//简单实现类
public class HelloServiceImpl implements HelloService {

    public String sayHello(String name) {
        return "hello,"+name;
    }

    public Person getPerson(String name) {
        Person person = new Person();
        person.setName(name);
        person.setAge(20);
        return person;
    }

}