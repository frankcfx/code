package com.frank.analyzer.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Test
{
    public static void main(String[] args) {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("请问我是否过了（true/false）：");
        String line = null;
        try {
            line = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (line.equals("true")){

            System.out.println("恭喜你过了！！！");
        }else {
            System.out.println("抱歉你没有通过面试。");
        }
    }
}
