package com.cfx.learningproject.generics;

public class GenericTest3 {

    public static void main(String[] args) {

        Box<String> name = new Box<String>("corn");
        Box<Integer> age = new Box<Integer>(712);

        System.out.println("name class:" + name.getClass());      // com.qqyumidi.Box
        System.out.println("age class:" + age.getClass());        // com.qqyumidi.Box
        System.out.println(name.getClass() == age.getClass());    // true

        Box<Integer> a = new Box<Integer>(712);
//        Box<Number> b = a;  // 1
        Box<Float> f = new Box<Float>(3.14f);
//        b.setData(f);        // 2

        Box<Number> number = new Box<Number>(314);

        getData(name);
        getData(age);
        getData(number);

//        getUpperNumberData(name); // 1
        getUpperNumberData(age);    // 2
        getUpperNumberData(number); // 3

    }

//    public static void getData(Box<Number> data) {
//        System.out.println("data :" + data.getData());
//    }

    // 类型通配符一般是使用 ? 代替具体的类型实参。注意了，此处是类型实参，而不是类型形参！
    // 且Box<?>在逻辑上是Box<Integer>、Box<Number>...等所有Box<具体类型实参>的父类。
    // 由此，我们依然可以定义泛型方法，来完成此类需求。
    public static void getData(Box<?> data) {
        System.out.println("data :" + data.getData());
    }

    public static void getUpperNumberData(Box<? extends Number> data){
        System.out.println("data :" + data.getData());
    }

}

class Box<T> {

    private T data;

    public Box() {

    }

    public Box(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}