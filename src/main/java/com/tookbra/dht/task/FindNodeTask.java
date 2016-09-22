package com.tookbra.dht.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tookbra.dht.*;
import com.tookbra.dht.bootstrap.DhtClient;
import com.tookbra.dht.common.KrpcUtil;
import com.tookbra.dht.domian.Message;
import com.tookbra.dht.exception.CounterException;
import com.tookbra.dht.redis.modal.CounterType;
import com.tookbra.dht.redis.service.CounterService;
import com.tookbra.dht.redis.service.impl.RedisServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by tookbra on 2016/7/28.
 */
public class FindNodeTask {
    private static final Logger logger = LoggerFactory.getLogger(FindNodeTask.class);
    private static final ScheduledExecutorService joinDhtThreadPool = Executors.newSingleThreadScheduledExecutor();
//    private static final Executor findNodeThreadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("findNode-%s").build());
    private static final ScheduledExecutorService findNodeThreadPool = Executors.newScheduledThreadPool(10,new ThreadFactoryBuilder().setNameFormat("findNode-%s").build());

    private static ScheduledFuture scheduledFuture;
    private List<String> seedNodes;

    @Autowired
    private DhtClient dhtClient;

    @Autowired
    RedisServiceImpl<Node> redisService;


    @Autowired
    CounterService counterService;

    //init node find
    @PostConstruct
    public void init() throws IOException, InterruptedException {
//        scheduledFuture = joinDhtThreadPool.scheduleWithFixedDelay(joinDht(),0, Constant.FinderDelayTime,TimeUnit.MILLISECONDS);
        joinDht();
        findNode();
    }

    //join int dht
    private Runnable joinDht() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                for (String seedNode : seedNodes) {
                    Node node = new Node(null, seedNode, 6881);
                    if (dhtClient.channel.isActive() && dhtClient.channel.isWritable()) {
//              Constant.countFindRequest.incrementAndGet();
                        try {
                            dhtClient.channel.write(new Message(KrpcUtil.findNode(null),node));
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        return runnable;
    }

    //node find
    private void findNode() throws IOException {
        scheduledFuture = findNodeThreadPool.scheduleWithFixedDelay(joinDht(),0, Constant.FinderDelayTime,TimeUnit.MILLISECONDS);
    }


    private static int i=0;

    private Runnable findNodeDelay() {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    Node node = (Node) redisService.leftPop("node");
//                    i++;
//                    logger.info(String.valueOf(i));
                    if(node != null) {
                        counterService.increment(Constant.COUNTER_NMAE, CounterType.COUNT_FIND_REQUEST);
                        dhtClient.channel.writeAndFlush(new Message(KrpcUtil.findNode(node.getId()), node));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (CounterException e) {
                    e.printStackTrace();
                }
            }
        };
        return runnable;
    }

    public void changeDelay(long delay) {
        scheduledFuture.cancel(true);
        if(scheduledFuture.isCancelled()) {
            scheduledFuture = findNodeThreadPool.scheduleWithFixedDelay(findNodeDelay(), 0, delay, TimeUnit.MILLISECONDS);
        }
    }

    public List<String> getSeedNodes() {
        return seedNodes;
    }

    public void setSeedNodes(List<String> seedNodes) {
        this.seedNodes = seedNodes;
    }
}
