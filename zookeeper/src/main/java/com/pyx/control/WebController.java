package com.pyx.control;

import com.pyx.curator.CuratorFactory;
import com.pyx.lock.NoFairLockDriver;
import com.pyx.util.ZookeeperSimple;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(description = "控制器", tags = "TEST")
@RestController
public class WebController {

	private Logger logger = LoggerFactory.getLogger(WebController.class);

    /*@Autowired
    private ZooKeeperWatcher zkWatch;*/

    /*@Autowired
    private ZookeeperSimple zkWatch;*/

    @Autowired
    private CuratorFactory zkWatch;

    @ApiOperation("1、创建目录")
	@RequestMapping(value = "/addRootNode", method = RequestMethod.GET)
	public boolean addRootNode(@RequestParam("nodeMame") String nodeMame, @RequestParam("data") String data) {
        //设置监控，因为watch只有一次性，所以每次执行zk.XXX前都需要监控
        //zkWatch.exists(nodeMame, true);
        return zkWatch.createPath(nodeMame, data);
	}


    @ApiOperation("2、查询数据")
    @RequestMapping(value = "/getNodeData", method = RequestMethod.GET)
    public String getNodeData(@RequestParam("nodeMame") String nodeMame) {
        String data = zkWatch.readData(nodeMame, true);
        return data;
    }

    @ApiOperation("3、更新数据")
    @RequestMapping(value = "/writeData", method = RequestMethod.GET)
    public boolean writeData(String path, String data) {
        //zkWatch.exists(path, true);
        return zkWatch.writeData(path, data);
    }


    @ApiOperation("4、获取子节点数据 ")
    @RequestMapping(value = "/getChildren", method = RequestMethod.GET)
    private List<String> getChildren(String path) {
        List<String> ret = zkWatch.getChildren(path, true);
        return ret;
    }

    @ApiOperation("5、测试并发创建节点--可以用来作为并发锁（节点是临时节点，创建成功的线程可以执行并发任务，任务完成后删除临时节点好释放锁） ")
    @RequestMapping(value = "/concurrentAddNode", method = RequestMethod.GET)
    private boolean concurrentAddNode(String path) {
        for (int i=0;i<100;i++) {
            new Thread(() -> {
                boolean tempRet = zkWatch.createPath(path, "piyingxu");
                logger.info("tempRet={}", tempRet);
            }, "线程" + i).start();
        }
        //1、100个并发创建只会有一个成功返回，这种场景可以用于做分布式锁，创建节点的类型用临时类型，用完删除掉自己创建的临时目录节点就释放出锁
        //2、对于第二类， /path 已经预先存在，所有客户端在它下面创建临时顺序编号目录节点，和选master一样，编号最小的获得锁，用完删除，依次方便。
        return true;
    }
/*

    @ApiOperation("5、测试并发锁InterProcessMutex")
    @RequestMapping(value = "/testLock", method = RequestMethod.GET)
    private boolean testLock(String path) {
        for (int i=0;i<20;i++) {
            new Thread(() -> {
                logger.info("=======> 我在等待取锁，任务正在排队");
                InterProcessMutex lock = new InterProcessMutex(zkWatch.getClient(), "/mylock"); //有序锁--先来先得锁（公平锁）
                // InterProcessMutex lock = new InterProcessMutex(zkWatch.getClient(),"/mylock", new NoFairLockDriver());//无序锁--随机得锁（非公平锁）
                try {
                    //阻塞方法，获取不到锁线程会挂起。
                    lock.acquire();
                    logger.info("=======> 已经获取到锁,任务处理开始");
                    Thread.sleep(3000);//模拟处理任务耗时
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
                logger.info("<======= 锁已经被释放,任务处理接收");
            }, "线程" + i).start();
        }
        return true;
    }
*/

}

