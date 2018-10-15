package com.frank.analyzer.kafka;

import org.apache.log4j.Logger;

/**
 * Created by ibf on 10/21.
 */
public class Log4jTest {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger(Log4jTest.class);

        // 如果是异步发送数据，那么如果最后一个批次中，数据还没法发送成功，程序就已经关闭了，这样就会到时最终可能出现数据丢失的情况
        for (int i = 0; i < 60; i++) {
            logger.debug("debug-" + i);
            logger.info("info-" + i);
            logger.warn("warn-" + i);
            logger.error("error-" + i);
            logger.fatal("fatal-" + i);
        }
    }
}
