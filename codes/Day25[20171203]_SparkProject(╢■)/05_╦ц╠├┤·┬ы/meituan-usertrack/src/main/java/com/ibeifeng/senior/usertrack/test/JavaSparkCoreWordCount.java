package com.ibeifeng.senior.usertrack.test;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.*;
import org.apache.spark.rdd.RDD;
import scala.Tuple2;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by ibf on 12/03.
 */
public class JavaSparkCoreWordCount {
    public static void main(String[] args) {
        /**
         * Java中Spark编程的程序入口：
         * SparkCore:
         * --JavaSparkContext
         * --JavaRDD、JavaPairRDD
         * SparkSQL:
         * --SQLContext、HiveContext
         * --DataFrame
         * SparkStreaming:
         * --JavaStreamingContext
         * --JavaDStream、JavaPairDStream
         * NOTE: RDD和JavaRDD之间是可以互相转换的
         */
        // 1. 上下文的构建
        SparkConf conf = new SparkConf()
                .setMaster("local[*]")
                .setAppName("wordcount");
        JavaSparkContext jsc = new JavaSparkContext(conf);
        // 保证一个jvm中只会存在一个SparkContext对象
        // JavaSparkContext jsc =  JavaSparkContext.fromSparkContext(SparkContext.getOrCreate(conf));

        // 2. RDD数据读取
        String path = "data/wc.txt";
        JavaRDD<String> rdd = jsc.textFile(path);

        // 3. RDD的操作, scala中实现代码: rdd.flatMap(_.split(" ")).filter(_.nonEmpty).map((_,1)).reduceByKey(_+_)
        // NOTE: 在scala中，对于key/value类型的RDD，可以直接调用reduceByKey类型的API，原因是：scala中存在着隐式转换，自动将RDD转换为PairRDDFunctions
        // NOTE: 但是在java中，不存在隐式转换的，所以JavaRDD对象是不能直接调用reduceByKey类型的API，需要先将JavaRDD转换为JavaPairRDD
        JavaPairRDD<String, Integer> wordCountRDD = rdd
                .flatMap(new FlatMapFunction<String, String>() {

                    @Override
                    public Iterable<String> call(String s) throws Exception {
                        String[] arr = s.split(" ");
                        // JAVA中数组不是Iterable对象，所以需要转换为可迭代对象再返回
                        return Arrays.asList(arr);
                    }
                })
                .filter(new Function<String, Boolean>() {
                    @Override
                    public Boolean call(String v1) throws Exception {
                        return v1 != null && v1.trim().length() > 0;
                    }
                })
                .map(new Function<String, Tuple2<String, Integer>>() {
                    @Override
                    public Tuple2<String, Integer> call(String v1) throws Exception {
                        return new Tuple2<String, Integer>(v1, 1);
                    }
                })
                .mapToPair(new PairFunction<Tuple2<String, Integer>, String, Integer>() {
                    @Override
                    public Tuple2<String, Integer> call(Tuple2<String, Integer> stringIntegerTuple2) throws Exception {
                        return stringIntegerTuple2;
                    }
                })
                .reduceByKey(new Function2<Integer, Integer, Integer>() {
                    @Override
                    public Integer call(Integer v1, Integer v2) throws Exception {
                        return v1 + v2;
                    }
                });

        // 3. 结果输出
        wordCountRDD.foreachPartition(new VoidFunction<Iterator<Tuple2<String, Integer>>>() {
            @Override
            public void call(Iterator<Tuple2<String, Integer>> tuple2Iterator) throws Exception {
                while (tuple2Iterator.hasNext()) {
                    Tuple2<String, Integer> item = tuple2Iterator.next();
                    System.out.println("key=" + item._1() + ", vlaue=" + item._2());
                }
            }
        });

        // JavaRDD -> JavaPairRDD使用mapToPair API
        // JavaPairRDD -> JavaRDD使用map API
        JavaRDD<String> rdd2 = wordCountRDD.map(new Function<Tuple2<String, Integer>, String>() {
            @Override
            public String call(Tuple2<String, Integer> v1) throws Exception {
                return null;
            }
        });
        // JavaRDD -> RDD使用rdd API
        RDD<String> rdd3 = rdd2.rdd();
        // RDD -> JavaRDD使用toJavaRDD API
        JavaRDD<String> rdd4 = rdd3.toJavaRDD();

        // TODO: 作业，使用top函数实现TopN的操作，获取出现次数最多的前3个单词
        // NOTE: 实现过程中，注意：比较器的序列化异常（有的时候不能直接使用匿名内部类）
    }
}
