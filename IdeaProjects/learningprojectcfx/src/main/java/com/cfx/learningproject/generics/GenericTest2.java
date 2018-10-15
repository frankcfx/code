package com.cfx.learningproject.generics;

import java.util.ArrayList;
import java.util.List;

public class GenericTest2 {

    public static void main(String[] args) {
        /*
        List list = new ArrayList();
        list.add("qqyumidi");
        list.add("corn");
        list.add(100);
        */

        List<String> list = new ArrayList<String>();
        list.add("qqyumidi");
        list.add("corn");
//        list.add(100);   // 1  提示编译错误

        for (int i = 0; i < list.size(); i++) {
            String name = list.get(i); // 2
            System.out.println("name:" + name);
        }

        String [] testStr= new String[10];

        System.out.println(testStr.length);

        String [] testStr2 = list.toArray(testStr);

        System.out.println(testStr.length);
        System.out.println(testStr2.length);

        System.out.println(testStr == testStr2);

        for(String str : testStr){
            System.out.println(str);
        }

        for(String str : testStr2){
            System.out.println(str);
        }

    }
}