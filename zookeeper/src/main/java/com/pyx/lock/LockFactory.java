package com.pyx.lock;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

/**
 * @author: yingxu.pi@transsnet.com
 * @date: 2018/11/13 11:52
 * 锁原理：
 *
 * 1、首先要创建一个锁的根节点，比如/mylock。
 * 2、想要获取锁的客户端在锁的根节点下面创建znode，作为/mylock的子节点，节点的类型要选择CreateMode.PERSISTENT_SEQUENTIAL，节点的名字最好用uuid
 * （至于为什么用uuid我后面会讲，先说一下~如果不这么做在某种情况下会发生死锁，这一点我看了很多国内朋友自己的实现，都没有考虑到这一层，这也是我为什么不建议大家自己去封装这种锁，因为它确实很复杂），
 * 假设目前同时有3个客户端想要获得锁，那么/mylock下的目录应该是这个样子的。
 * xxx-lock-0000000001，xxx-lock-0000000002，xxx-lock-0000000003
 * xxx为uuid ， 0000000001，0000000002，0000000003 是zook服务端自动生成的自增数字。
 *
 * 3、当前客户端通过getChildren（/mylock）获取所有子节点列表并根据自增数字排序，然后判断一下自己创建的节点的顺序是不是在列表当中最小的，如果是 那么获取到锁，如果不是，
 * 那么获取自己的前一个节点，并设置监听这个节点的变化，当节点变化时重新执行步骤3 直到自己是编号最小的一个为止。
 * 举例：假设当前客户端创建的节点是0000000002，因为它的编号不是最小的，所以获取不到锁，那么它就找到它前面的一个节点0000000001 并对它设置监听。
 *
 * 4、释放锁，当前获得锁的客户端在操作完成后删除自己创建的节点，这样会激发zook的事件给其它客户端知道，这样其它客户端会重新执行（步骤3）。
 * 举例：加入客户端0000000001获取到锁，然后客户端0000000002加入进来获取锁，发现自己不是编号最小的，那么它会监听它前面节点的事件（0000000001的事件）然后执行步骤（3），
 * 当客户端0000000001操作完成后删除自己的节点，这时zook服务端会发送事件，这时客户端0000000002会接收到该事件，然后重复步骤3直到获取到锁）
 *
 * 上面的步骤实现了一个有序锁，也就是先进入等待锁的客户端在锁可用时先获得锁。
 * 如果想要实现一个随机锁，那么只需要把PERSISTENT_SEQUENTIAL换成一个随机数即可---（非公平锁）。
 */
public class LockFactory {

    public void getLock (CuratorFramework client) {
        /**
         * 这个类是线程安全的，一个JVM创建一个就好
         * mylock 为锁的根目录，我们可以针对不同的业务创建不同的根目录
         */
        final InterProcessMutex lock = new InterProcessMutex(client, "/mylock");
        try {
            //阻塞方法，获取不到锁线程会挂起。
            lock.acquire();
            System.out.println("已经获取到锁");
            Thread.sleep(10000);//模拟处理任务耗时

        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            //释放锁，必须要放到finally里面，已确保上面方法出现异常时也能够释放锁。
            try {
                lock.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
