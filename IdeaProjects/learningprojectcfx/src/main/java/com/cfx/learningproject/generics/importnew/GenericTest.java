package com.cfx.learningproject.generics.importnew;

public class GenericTest {

    public static void main(String[] args) {

        Pair<Integer, String> p1 = new Pair<>(1, "apple");
        Pair<Integer, String> p2 = new Pair<>(2, "pear");
        boolean same = Util.<Integer, String>compare(p1, p2);
        //在Java1.7/1.8利用type inference，让Java自动推导出相应的类型参数
        boolean same2 = Util.compare(p1, p2);

        System.out.println(same);
        System.out.println(same2);

    }

    public void boxTest(Box<Number> n) { /* ... */ }


    public static <T> int countGreaterThan(T[] anArray, T elem) {
        int count = 0;
        for (T e : anArray)
            //这样很明显是错误的，因为除了short, int, double, long, float, byte, char等原始类型，
            // 其他的类并不一定能使用操作符>，所以编译器报错，那怎么解决这个问题呢？答案是使用边界符
//            if (e > elem)  // compiler error
                ++count;
        return count;
    }

    // 做一个类似于下面这样的声明，这样就等于告诉编译器类型参数T代表的都是实现了Comparable接口的类，
    // 这样等于告诉编译器它们都至少实现了compareTo方法。
    //public class Name implements Comparable<Name> {
    //   ...
    //   public int compareTo(Name n) { ... }
    //}
    public static <T extends Comparable<T>> int countGreaterThan(T[] anArray, T elem) {

        int count = 0;
        for (T e : anArray)
            if (e.compareTo(elem) > 0)
                ++count;
        return count;
    }

    //T extends Comparable 不是范型写法，老的语法
    public static <T extends Comparable> int countGreaterThan2(T[] anArray, T elem) {

        int count = 0;
        for (T e : anArray)
            if (e.compareTo(elem) > 0)
                ++count;
        return count;
    }

}


class Util {

    public static <K, V> boolean compare(Pair<K, V> p1, Pair<K, V> p2) {
        return p1.getKey().equals(p2.getKey()) &&
                p1.getValue().equals(p2.getValue());
    }
}

class Pair<K, V> {

    private K key;

    private V value;


    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }


    public void setKey(K key) {
        this.key = key;
    }

    public void setValue(V value) {
        this.value = value;
    }


    public K getKey()

    {
        return key;
    }

    public V getValue() {
        return value;
    }
}
