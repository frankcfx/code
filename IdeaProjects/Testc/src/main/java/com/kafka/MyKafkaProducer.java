package com.kafka;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import java.util.Properties;
import java.util.UUID;


public class MyKafkaProducer {
    public static void main(String[] args) {
        String mytopic = "mykafka2";
        Properties props = new Properties();
        String brokerList = "kafka-01:9092,kafka-01:9093,kafka-01:9094,kafka-01:9095";

        props.put("serializer.class", "kafka.serializer.StringEncoder");

        props.put("metadata.broker.list", brokerList);


        props.put("request.required.acks", "1");
        props.put("partitioner.class", "kafka.producer.DefaultPartitioner");
        Producer<String, String> producer = new Producer<String, String>(new ProducerConfig(props));

        for (int index = 0; index < 88; index++) {
            producer.send(new KeyedMessage<String, String>(mytopic, index + "", UUID.randomUUID() + ""));
        }
    }
}