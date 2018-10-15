package com.frank.analyzer.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;
import java.util.Random;

public class ProducerDemo {
    public static void main(String[] args) {
        final char[] chars = "qazwsxedcrfvtgbyhnujmikolp".toCharArray();
        final int charLength = chars.length;
        final String topicName = "beifeng0";

        // 1. 创建一个Producer对象
        // 1.1 构建一个Properties对象，并且给定连接kafka的相关Producer参数
        Properties props = new Properties();
        // a. 给定kafka服务器的路径信息
        props.put("metadata.broker.list", "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095");
        // b. 给定数据发送是否等待broker返回，0表示不返回
        props.put("request.required.acks", "0");
        // c. 给定数据发送方式，默认为sync(同步), 可选async
        props.put("producer.type", "sync");
        // d. 给定消费序列化为byte数组的方式，默认为kafka.serializer.DefaultEncoder
        // 默认情况下，要求producer发送的消息中的类型是Byte数组，如果发送string类型数据，一般都需要修改该参数
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        // e. 给定数据分区器，决定数据发送到那一个分区中
        // 默认的是kafka.producer.DefaultPartitioner, 采用key的hash值决定数据到那一个分区
//        props.put("partitioner.class", "com.frank.analyzer.kafka.PartitionerDemo");

        // 1.2 构建ProducerConfig对象
        ProducerConfig config = new ProducerConfig(props);
        // 1.3 构建Producer对象
        final Producer<String, String> producer = new Producer<String, String>(config);

        // 2. 以多线程的形式发送数据
        final Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 3; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // 初始化一个发送数据条数的值
                    int events = random.nextInt(1000) + 100;
                    String threadName = Thread.currentThread().getName();
                    for (int j = 0; j < events; j++) {
                        if (j != 0 && j % 100 == 0) {
                            System.out.println("线程[" + threadName + "]已经发送" + j + "条数据!");
                        }

                        // 1. 构建发送发送的KeyedMessage对象
                        String key = "key_" + random.nextInt(100);
                        // 假设value是由单词构成的，单词数量范围为[1,10]
                        StringBuilder sb = new StringBuilder();
                        int wordNums = random.nextInt(10) + 1;
                        for (int k = 0; k < wordNums; k++) {
                            // 每个单词由单个的字符构成，假设每个单词中字符的数量为[2,5]
                            StringBuilder sb2 = new StringBuilder();
                            int charNums = random.nextInt(4) + 2;
                            for (int l = 0; l < charNums; l++) {
                                sb2.append(chars[random.nextInt(charLength)]);
                            }
                            sb.append(sb2.toString()).append(" ");
                        }
                        String value = sb.toString().trim();

                        // 2. kafka消息构建及发送
                        KeyedMessage<String, String> message = new KeyedMessage<String, String>(topicName, key, value);
                        producer.send(message);

                        // 3. 休息一下
                        try {
                            Thread.sleep(random.nextInt(50) + 10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("线程[" + threadName + "]已经发送完了，总共" + events + "条数据!!!");
                }
            }, "Thread-" + i).start();
        }

        // 3. 关闭生产者的连接
        // 要求数据发送完成后，进行关闭操作 ==> 一般就可以考虑添加一个jvm钩子，当jvm退出的时候进行数据关闭操作
        Runtime.getRuntime().addShutdownHook(new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("关闭producer......");
                        producer.close();
                    }
                }
        ));

    }
}
