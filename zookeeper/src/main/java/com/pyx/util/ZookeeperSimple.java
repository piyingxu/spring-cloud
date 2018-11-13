package com.pyx.util;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/9 9:47
 */

@Component
public class ZookeeperSimple {

    private static final Logger log = LoggerFactory.getLogger(ZookeeperSimple.class);

    private static ZooKeeper zk;

    /**
     * zookeeper地址
     */
    private static final String CONNECT_ADDR = "127.0.0.1:2181";

     /**
     * session超时时间，
     */
    private static final int SESSION_OUTTIME = 2000;//ms


    public ZookeeperSimple() {
        // 创建一个与服务器的连接 需要(服务端的 ip+端口号)(session过期时间)(Watcher监听注册)
        try {
           /*
            参数1：Zookeeper集群的服务器地址列表
                该地址是可以填写多个的，以逗号分隔。如"127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183",那客户端连接的时候到底是使用哪一个呢？先随机打乱，然后轮询着用，后面再详细介绍。
            参数2：sessionTimeout
                最终会引出三个时间设置：和服务器端协商后的sessionTimeout、readTimeout、connectTimeout。
                服务器端使用协商后的sessionTimeout：即超过该时间后，客户端没有向服务器端发送任何请求（正常情况下客户端会每隔一段时间发送心跳请求，此时服务器端会从新计算客户端的超时时间点的），则服务器端认为session超时，清理数据。此时客户端的ZooKeeper对象就不再起作用了，需要再重新new一个新的对象了。
                客户端使用connectTimeout、readTimeout分别用于检测连接超时和读取超时，一旦超时，则该客户端认为该服务器不稳定，就会从新连接下一个服务器地址。
            参数3：Watcher
                作为ZooKeeper对象一个默认的Watcher，用于接收一些事件通知。如和服务器连接成功的通知、断开连接的通知、Session过期的通知等。同时我们可以看到，一旦和ZooKeeper服务器连接建立成功，就会获取服务器端分配的sessionId和password，如下：
                sessionId=94249128002584594
                password=[B@4de3aaf6
            */
            zk = new ZooKeeper(CONNECT_ADDR, SESSION_OUTTIME, new Watcher() {
                public void process(WatchedEvent event) { // 监控所有被触发的事件(一次性有效，必须再次注册 watcher)
                    //获取事件的状态
                    try {
                        Event.KeeperState keeperState = event.getState();
                        Event.EventType eventType = event.getType(); // 事件类型(节点的创建与删除、数据改变，子节点改变)
                        String path = event.getPath(); // 节点路径
                        String logPrefix = "收到Watcher通知: ";
                        log.info(logPrefix + ": path=" + path + ", type=" + eventType);

                        //如果是建立连接
                        if (keeperState.SyncConnected == keeperState) {
                            if (Event.EventType.None == eventType) {
                                //如果建立连接成功，则发送信号量，让后续阻塞程序向下执行
                                log.info(logPrefix + "zk 建立连接");
                            } else if (Event.EventType.NodeCreated == eventType) {
                                log.info(logPrefix + "节点建立");
                            }  else if (Event.EventType.NodeChildrenChanged == eventType) {
                                log.info(logPrefix + "子节点变更");
                            }else if (Event.EventType.NodeDataChanged == eventType) {
                                log.info(logPrefix + "数据变更");
                            }
                        } else if (Event.KeeperState.Disconnected == keeperState) {
                            log.info(logPrefix + "与ZK服务器断开连接");
                        } else if (Event.KeeperState.AuthFailed == keeperState) {
                            log.info(logPrefix + "权限检查失败");
                        } else if (Event.KeeperState.Expired == keeperState) {
                            log.info(logPrefix + "会话失效");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            //监听这个节点
            zk.exists("/pyx", true);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

   /**
     * 创建一个目录节点
     * CreateMode:
     * PERSISTENT (持续的，相对于EPHEMERAL，不会随着client的断开而消失)
     * PERSISTENT_SEQUENTIAL（持久的且带顺序的）
     * EPHEMERAL (短暂的，生命周期依赖于client session)
     * EPHEMERAL_SEQUENTIAL  (短暂的，带顺序的)
     */

    public boolean createPath(String nodeMame, String data) {
        boolean ret = false;
        try {
            log.info("收到创建目录请求,nodeMame={},data={}", nodeMame, data);

            // 创建节点
            List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE; // 访问权限(开放，所有人可访问)
            ///CreateMode createMode = CreateMode.PERSISTENT; // 节点类型(持久节点，客户端连接断开后该节点不会删除，临时节点会被删除)
            CreateMode createMode = CreateMode.EPHEMERAL; //临时有序节点
            String path = zk.create(nodeMame, data.getBytes(), acl, createMode);
            System.out.println("生成节点：" + path);
            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public String readData(String path, boolean needWatch) {
        try {
            log.info("sessionId={}, sessionPwd={}, sessionTimeOut={}", zk.getSessionId(), zk.getSessionPasswd(), zk.getSessionTimeout());
            return new String(this.zk.getData(path, needWatch, null));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 更新指定节点数据内容
     * @param path 节点路径
     * @param data 数据内容
     * @return
     */
    public boolean writeData(String path, String data) {
        try {
            this.zk.setData(path, data.getBytes(), -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public Stat exists(String path, boolean needWatch) {
        try {
            return this.zk.exists(path, needWatch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getChildren(String path, boolean needWatch) {
        try {
            return this.zk.getChildren(path, needWatch);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

