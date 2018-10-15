package com.cfx.learningproject.generics;

import org.apache.log4j.Logger;

public class Log {

    public static Logger logger = Logger.getLogger(Log.class);

    public static void d (String name, String value) {

        logger.info(name + "" + value);

    }

    public static void d (String name, int value) {

        logger.info(name + "" + value);

    }
}
