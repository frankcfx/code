package com.jeremy.stream;

import java.io.*;
public class TestBufferStream1{

    public static void main(String args[]){
        try{
            BufferedWriter bw = new BufferedWriter(new FileWriter("/home/cfx/IdeaProjects/jeremyprocess/out/dat.txt"));
            //在节点流FileWriter的外面再套一层处理流BufferedWriter
            String s = null;
            for(int i=0;i<100;i++){
                s = String.valueOf(Math.random());//“Math.random()”将会生成一系列介于0～1之间的随机数。
                // static String valueOf(double d)这个valueOf()方法的作用就是把一个double类型的数转换成字符串
                //valueOf()是一个静态方法，所以可以使用“类型.静态方法名”的形式来调用
                bw.write(s);//把随机数字符串写入到指定文件中
                bw.newLine();//调用newLine()方法使得每写入一个随机数就换行显示
            }
            bw.flush();//调用flush()方法清空缓冲区

            BufferedReader br = new BufferedReader(new FileReader("/home/cfx/IdeaProjects/jeremyprocess/out/dat.txt"));
            //在节点流FileReader的外面再套一层处理流BufferedReader
            while((s = br.readLine())!=null){
                //使用BufferedReader处理流里面提供String readLine()方法读取文件中的数据时是一行一行读取的
                //循环结束的条件就是使用readLine()方法读取数据返回的字符串为空值后则表示已经读取到文件的末尾了。
                System.out.println(s);

            }
            bw.close();
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
