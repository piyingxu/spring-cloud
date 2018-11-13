package com.pyx.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/12 10:27
 */
public class Test {

    private static final Logger log = LoggerFactory.getLogger(Test.class);

    public static void  main (String str[]) {
        new Thread(() -> {
            log.info("123");
        }).start();
    }
}
