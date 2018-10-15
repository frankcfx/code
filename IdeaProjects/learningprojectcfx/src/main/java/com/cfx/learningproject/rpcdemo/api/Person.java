package com.cfx.learningproject.rpcdemo.api;

import java.io.Serializable;
//普通公共bean
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;
    private String name;
    private Integer age;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    @Override
    public String toString() {
        return "Person [name=" + name + ", age=" + age + "]";
    }
}
