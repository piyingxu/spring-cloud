package com.pyx.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/12 13:56
 */
@Component
public class CuratorFactory {

    private static final Logger log = LoggerFactory.getLogger(CuratorFactory.class);

    /**
     * zookeeper服务器地址
     */
    //private static final String CONNECTION_ADDR = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
    private static final String CONNECTION_ADDR = "127.0.0.1:2181";

    /**
     * session超时时间，由于网络恢复后，客户端可能会重新连接上服务器，但是很不幸，服务器会告诉客户端一个异常：SESSIONEXPIRED（会话过期）
     */
    private static final int SESSION_OUTTIME = 2000;//ms

    /**
     * 连接超时时间，ZK客户端捕获“连接断开”异常 ——> 获取一个新的ZK地址 ——> 尝试连接
     */
    private static final int CONNECT_OUTTIME = 2000;//ms

    private CuratorFramework client;

    private CuratorFramework clientTwo;

    public CuratorFactory() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);//刚开始重试间隔为1秒，之后重试间隔逐渐增加，最多重试不超过三次

        //RetryPolicy retryPolicy1 = new RetryNTimes(3, 1000);//最大重试次数，和两次重试间隔时间
        //RetryPolicy retryPolicy2 = new RetryUntilElapsed(5000, 1000);//会一直重试直到达到规定时间，第一个参数整个重试不能超过时间，第二个参数重试间隔

        //第一种方式
        client = CuratorFrameworkFactory.builder().connectString(CONNECTION_ADDR)
                .sessionTimeoutMs(SESSION_OUTTIME)//会话超时时间
                .connectionTimeoutMs(5000)//连接超时时间
                .retryPolicy(retryPolicy)
                .build();
        client.start();

        //第二种方式
        clientTwo = CuratorFrameworkFactory.newClient(CONNECTION_ADDR, SESSION_OUTTIME, CONNECT_OUTTIME, retryPolicy);
        clientTwo.start();

        myCuratorListener(client);

        getChildren("/pyx", true); //子节点变化只有调用getChildren,父节点才会收的到

        log.info("client={},clientTwo={}", client, clientTwo);
    }


    public boolean createPath(String nodeMame, String data) {
        boolean ret = false;
        try {
            Stat stat = exists(nodeMame);
            String path = client.create().creatingParentsIfNeeded()//若创建节点的父节点不存在会先创建父节点再创建子节点
                    .withMode(CreateMode.PERSISTENT)//withMode节点类型，
                    .inBackground()
                    .forPath(nodeMame, data.getBytes());
            ret = true;
            nodeCacheListener(client, nodeMame);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String readData(String path, boolean needWatch) {
        try {
            //exists(path);
            //获取数据不能调用inBackground(),否则为null
            byte[] content = client.getData().forPath(path);
            return new String(content);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean writeData(String path, String data) {
        try {
            exists(path);
            client.setData().inBackground().forPath(path, data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<String> getChildren(String path, boolean needWatch) {
        try {
            exists(path);
            return client.getChildren().usingWatcher(
                    new Watcher() {
                        @Override
                        public void process(WatchedEvent watchedEvent) {
                            doMyWatch(watchedEvent);
                        }
                    }
            ).inBackground().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 第一种监控方式，通过每次操作前调用checkExists方法
     *
     * @param path
     */
    public Stat exists(String path) {
        Stat stat = null;
        try {
            stat = client.checkExists().usingWatcher(new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    doMyWatch(watchedEvent);
                }
            }).forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stat;
    }

    private void doMyWatch(WatchedEvent event) { // 监控所有被触发的事件(一次性有效，必须再次注册 watcher)
        log.info("进入 process 。。。。。event = " + event);

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (event == null) {
            return;
        }

        // 连接状态
        Watcher.Event.KeeperState keeperState = event.getState();
        // 事件类型
        Watcher.Event.EventType eventType = event.getType();
        // 受影响的path
        String path = event.getPath();

        String logPrefix = "【Watcher】";

        log.info(logPrefix + "收到Watcher通知");
        log.info(logPrefix + "连接状态:\t" + keeperState.toString());
        log.info(logPrefix + "事件类型:\t" + eventType.toString());
        log.info(logPrefix + "path:\t" + path);

        if (Watcher.Event.KeeperState.SyncConnected == keeperState) {
            // 成功连接上ZK服务器
            if (Watcher.Event.EventType.None == eventType) {
                log.info(logPrefix + "成功连接上ZK服务器");
            }
            //创建节点
            else if (Watcher.Event.EventType.NodeCreated == eventType) {
                log.info(logPrefix + "节点创建");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //更新节点
            else if (Watcher.Event.EventType.NodeDataChanged == eventType) {
                log.info(logPrefix + "节点数据更新");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            //更新子节点
            else if (Watcher.Event.EventType.NodeChildrenChanged == eventType) {
                log.info(logPrefix + "子节点变更");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                log.info(logPrefix + "子节点列表：" + this.getChildren(path, true));
            }
            //删除节点
            else if (Watcher.Event.EventType.NodeDeleted == eventType) {
                log.info(logPrefix + "节点 " + path + " 被删除");
            } else ;
        } else if (Watcher.Event.KeeperState.Disconnected == keeperState) {
            log.info(logPrefix + "与ZK服务器断开连接");
        } else if (Watcher.Event.KeeperState.AuthFailed == keeperState) {
            log.info(logPrefix + "权限检查失败");
        } else if (Watcher.Event.KeeperState.Expired == keeperState) {
            log.info(logPrefix + "会话失效");
        } else ;

        log.info("--------------------------------------------");
    }

    /**
     * 第二种监听方式，以inBackground()+CuratorListener这种方式来使用异步API, 在所以操作调用forPath之前调用inBackground()
     *
     * @author: yingxu.pi@transsnet.com
     * @date: 2018/11/12 16:19
     */
    public void myCuratorListener(CuratorFramework client) {
        try {
            CuratorListener listener = new CuratorListener() {
                @Override
                public void eventReceived(CuratorFramework client, CuratorEvent event) throws Exception {
                    log.info("=============== 监听事件触发，client={}, event内容为={}", client, event);
                }
            };
            client.getCuratorListenable().addListener(listener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void nodeCacheListener(CuratorFramework client, String path) {
        try {
            final NodeCache nodeCache = new NodeCache(client, path);
            nodeCache.start(true); //首次启动时就会缓存节点内容到Cache中
            nodeCache.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    System.out.println("监听事件触发");
                    System.out.println("重新获得节点内容为：" + new String(nodeCache.getCurrentData().getData()));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CuratorFramework getClient() {
        return client;
    }

    public void setClient(CuratorFramework client) {
        this.client = client;
    }
}
