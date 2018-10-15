package com.jeremy.stream;


import java.io.*;
public class TestTransform2{
    public static void main(String args[]){
        try{
            InputStreamReader isr = new InputStreamReader(System.in);
            //System.in这里的in是一个标准的输入流，用来接收从键盘输入的数据
            BufferedReader br = new BufferedReader(isr);
            String s = null;
            s = br.readLine();//使用readLine()方法把读取到的一行字符串保存到字符串变量s中去
            while(s != null){
                System.out.println(s.toUpperCase());//把保存在内存s中的字符串打印出来
                s = br.readLine();//在循环体内继续接收从键盘的输入
                if(s.equalsIgnoreCase("exit")){
                    //只要输入exit循环就结束，就会退出
                    break;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
