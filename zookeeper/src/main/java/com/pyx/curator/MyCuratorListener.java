package com.pyx.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/12 16:02
 */
public class MyCuratorListener {

    private static final Logger log = LoggerFactory.getLogger(MyCuratorListener.class);

    public MyCuratorListener(CuratorFramework client) {
        try {
            CuratorListener listener = new CuratorListener() {
                @Override
                public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                    log.info("监听事件触发，event内容为={}", event);
                }
            };
            client.getCuratorListenable().addListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
