package com.pyx.soa;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.CountDownLatch;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/13 10:17
 */
@Component
public class ServiceRegistryImpl implements ServiceRegistry, Watcher, ServletContextListener {

    private static Logger logger = LoggerFactory.getLogger(ServiceRegistryImpl.class);

    private static CountDownLatch latch = new CountDownLatch(1);

    private static final String CONNECT_ADDR = "127.0.0.1:2181";

    private ZooKeeper zk;

    private static final int SESSION_TIMEOUT = 5000;

    //服务器注册节点路径信息
    private static final String REGISTRY_PATH = "/registry";

    @Value("${server.port}")
    private int serverPort;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${spring.application.name}")
    private String serverName;

    public ServiceRegistryImpl() {
        try {
            zk = new ZooKeeper(CONNECT_ADDR, SESSION_TIMEOUT, this);
            latch.await(); //阻塞程序，如果连接成功则释放
            logger.info("connected to zookeeper");
        } catch (Exception ex) {
            logger.error("create zookeeper client failure", ex);
        }
    }

    @Override
    public void register(String serviceName, String serviceAddress) {
        try {
            String registryPath = REGISTRY_PATH;
            if (zk.exists(registryPath, false) == null) {
                zk.create(registryPath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.info("create registry node:{}", registryPath);
            }
            //创建服务节点（持久节点）
            String servicePath = registryPath + "/" + serviceName;
            if (zk.exists(servicePath, false) == null) {
                zk.create(servicePath, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                logger.info("create service node:{}", servicePath);
            }
            //创建地址节点
            String addressPath = servicePath + "/address-";
            String addressNode = zk.create(addressPath, serviceAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("create address node:{} => {}", addressNode, serviceAddress);
        } catch (Exception e) {
            logger.error("create node failure", e);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == Event.KeeperState.SyncConnected) {
            logger.info("zk 连接服务器成功");
            latch.countDown();
        }
    }


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        register(serverName, String.format("%s:%d",serverAddress,serverPort));
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
