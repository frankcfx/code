package com.cfx.learningproject.generics.importnew;

//public class Box {
//    private String object;
//
//    public void set(String object) {
//        this.object = object;
//    }
//
//    public String get() {
//        return object;
//    }
//}

public class Box<T> {
    // T stands for "Type"
    private T t;

    public void set(T t) {
        this.t = t;
    }

    public T get() {
        return t;
    }

    public static void main(String[] args) {

        Box<Integer> integerBox = new Box<Integer>();
        Box<Double> doubleBox = new Box<Double>();
        Box<String> stringBox = new Box<String>();

    }

}