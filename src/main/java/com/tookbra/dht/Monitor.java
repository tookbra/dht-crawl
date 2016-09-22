package com.tookbra.dht;

import com.tookbra.dht.task.FindNodeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.TimeUnit;

/**
 * @author tookbra
 * @date 2016/9/6
 */
public class Monitor implements Runnable  {

    private static final Logger logger = LoggerFactory.getLogger(Monitor.class);

    private FindNodeTask findNodeTask;


    public Monitor() {
    }

    public Monitor(FindNodeTask findNodeTask) {
        this.findNodeTask = findNodeTask;
    }

    @Override
    public void run() {
        while(!Thread.interrupted()) {
//            this.adjustFinderSpeed();
            /*logger.info("发出find_node请求数量:{}", Constant.countFindRequest);
            logger.info("收到find_node回复数量:{}", Constant.countFindResponse);
            logger.info("收到find_node请求数量:{}", Constant.countFindNode);
            logger.info("收到ping请求数量:{}", Constant.countPing);
            logger.info("收到announce_peer请求数量:{}", Constant.countAnnounce);
            logger.info("收到get_peers请求数量:{}", Constant.preCountGetPeers);
            logger.info("-----------------------");
            try {
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }

    private void adjustFinderSpeed() {
        //long count = Constant.countGetPeers.longValue() - Constant.preCountGetPeers.longValue();
        //long delay = count / 10000;
        //findNodeTask.changeDelay(Constant.FinderDelayTime + (delay * 100));
    }

    public FindNodeTask getFindNodeTask() {
        return findNodeTask;
    }

    public void setFindNodeTask(FindNodeTask findNodeTask) {
        this.findNodeTask = findNodeTask;
    }
}
