package com.frank.analyzer.kafka;

import kafka.producer.Partitioner;
import kafka.utils.VerifiableProperties;

/**
 * Created by ibf on 10/15.
 */
public class PartitionerDemo implements Partitioner {
    public PartitionerDemo(VerifiableProperties properties) {
        // 该构造函数必须给定
        // properties中存储的是producer连接kafka的相关配置参数
    }

    @Override
    public int partition(Object key, int numPartitions) {
        // 该API返回的就是数据key到底发送到那一个分区中，分区索引从0开始；numPartitions表示要发送到对应topic的总分区数量
        // 所有数据都发送到分区0
        return 0;
    }
}