package com.frank.analyzer.kafka;

import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.message.MessageAndMetadata;
import kafka.serializer.Decoder;
import kafka.serializer.StringDecoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by ibf on 10/15.
 */
public class ConsumerDemo {
    private ConsumerConnector connector = null;
    private String topicName = null;
    private int numThreads = 0;

    public ConsumerDemo(String groupId, String zkUrl, boolean largest, String topicName, int numThreads) {
        this.topicName = topicName;
        this.numThreads = numThreads;

        // 创建并给定consumer连接参数
        Properties props = new Properties();
        // a. 给定所属的consumer group id
        props.put("group.id", groupId);
        // b. 给定zk的连接位置信息
        props.put("zookeeper.connect", zkUrl);
        // c. 给定自动提交的间隔时间
        props.put("auto.commit.interval.ms", "2000");
        // d. 给定初始化consumer的时候使用offset偏移量值(只在第一次consumer启动消费数据的时候有效)
        if (largest) {
            props.put("auto.offset.reset", "largest");
        } else {
            props.put("auto.offset.reset", "smallest");
        }
        // 创建Consumer上下文对象
        ConsumerConfig config = new ConsumerConfig(props);
        // 创建Consumer连接对象
        this.connector = Consumer.createJavaConsumerConnector(config);
    }

    public void run() {
        // TODO: topicCountMap给定消费者消费的Topic名称以及消费该Topic需要使用多少个线程进行数据消费
        // 一个消费者可以消费多个Topic
        // topicCountMap以topic的名称为key，以线程数为value
        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topicName, numThreads);
        Decoder<String> keyDecoder = new StringDecoder(null);
        Decoder<String> valueDecoder = new StringDecoder(null);

        // 2. 根据参数创建一个数据读取流
        // TODO: 该API返回的集合是一个以Topic名称为Key，以该Topic的数据读取流为集合的value的Map集合
        // TODO: List<KafkaStream<String, String>> ===> 指的其实就是对应Topic消费的数据流，
        // TODO: 该List集合中的KafkaStream流的数目和给定的参数topicCountMap中对应topic的count数量一致；
        // TODO: 类似Consumer Group，一个线程/一个KafkaStream消费一个或者多个分区(>=0)的数据，一个分区的数据只被一个KafkaStream进行消费
        Map<String, List<KafkaStream<String, String>>> consumerStreamsMap = connector.createMessageStreams(topicCountMap, keyDecoder, valueDecoder);

        // 3. 获取对应topic的数据消费流
        List<KafkaStream<String, String>> streams = consumerStreamsMap.get(topicName);

        // 4. 数据消费
        int k = 0;
        for (final KafkaStream<String, String> stream : streams) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int count = 0;
                    String threadNames = Thread.currentThread().getName();
                    ConsumerIterator<String, String> iter = stream.iterator();
                    while (iter.hasNext()) {
                        // 获取数据
                        MessageAndMetadata<String, String> messageAndMetadata = iter.next();

                        // 处理数据
                        StringBuilder sb = new StringBuilder();
                        sb.append("Topic名称=").append(messageAndMetadata.topic());
                        sb.append("; key=").append(messageAndMetadata.key());
                        sb.append("; value=").append(messageAndMetadata.message());
                        sb.append("; partition ID=").append(messageAndMetadata.partition());
                        sb.append("; offset=").append(messageAndMetadata.offset());
                        System.out.println(sb.toString());
                        count++;
                    }
                    System.out.println("线程" + threadNames + "总共消费" + count + "条数据!!");
                }
            }, "Thread-" + k++).start();
        }
    }

    public void shutdown() {
        if (this.connector != null) {
            System.out.println("关闭consumer连接");
            this.connector.shutdown();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        String groupId = "o2o17_9";
        String zkUrl = "zookeeper1:2181/kafka07";
        boolean largest = true;
        String topicName = "beifeng0";
        int numThreads = 1;
        ConsumerDemo demo = new ConsumerDemo(groupId, zkUrl, largest, topicName, numThreads);

        demo.run();

        // 运行一段时间后进行关闭
        Thread.sleep(1000000);
//        Thread.sleep(10000000);

        demo.shutdown();
    }
}