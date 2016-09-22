package com.tookbra.dht.handler;

import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.tookbra.dht.Constant;
import com.tookbra.dht.Node;
import com.tookbra.dht.bootstrap.DhtClient;
import com.tookbra.dht.bootstrap.StoreClient;
import com.tookbra.dht.common.KrpcUtil;
import com.tookbra.dht.domian.Message;
import com.tookbra.dht.exception.CounterException;
import com.tookbra.dht.redis.modal.CounterType;
import com.tookbra.dht.redis.service.CounterService;
import com.tookbra.dht.redis.service.impl.RedisServiceImpl;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by tookbra on 2016/8/5.
 */
@Component
public class DhtHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(DhtHandler.class);

    private static final Executor findNodeThreadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("announcePeer-%s").build());
    private static final Executor downloadThreadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("download-%s").build());

    @Autowired
    StoreClient storeClient;

    @Autowired
    DhtClient dhtClient;

    @Autowired
    CounterService counterService;

    @Autowired
    RedisServiceImpl<Node> redisService;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message) throws Exception {
        Map<String,BEValue> dhtMap = message.getDhtMap();
        if(Objects.isNull(dhtMap.get("y")))
            return;
        if (dhtMap.get("y").getString().equals("q")) {
            Map<String, BEValue> subMap = dhtMap.get("a").getMap();
            message.getNode().setId(subMap.get("id").getBytes());
//            redisService.listLpush("node", message.getNode());
            switch (dhtMap.get("q").getString()) {
                case "ping":
                    onPing(channelHandlerContext, message);
                    break;
                case "get_peers":
                    byte [] info_hash = new byte[0];
                    if(subMap.containsKey("info_hash")) {
                        info_hash = subMap.get("info_hash").getBytes();
                    }
                    getPeers(channelHandlerContext,message,info_hash);
                    break;
                case "announce_peer":
                    announcePeer(channelHandlerContext,subMap, message);
                    break;
                case "find_nodes":
                    byte [] target = subMap.get("target").getBytes();
                    findNodes(channelHandlerContext,message, target);
                    break;
            }
        }
    }

    private void getPeers(ChannelHandlerContext channelHandlerContext, Message message, byte [] target) throws InvalidBEncodingException, CounterException {
        String y = "r";
        Map map = new HashMap<>();
        if(target.length == 0) {
            y = "e";
            List<Object> list = new ArrayList<>();
            list.add(203);
            list.add("`get_peers` missing required `a.info_hash` field");
            map.put("e", list);
        } else {
            map.put("id", KrpcUtil.getNeighbor(target));
            map.put("nodes", "");
            map.put("token", KrpcUtil.bytesToHexString(target).substring(2));
        }
        counterService.increment(Constant.COUNTER_NMAE, CounterType.COUNT_GET_PEERS);
        try {
            channelHandlerContext.channel().write(new Message(KrpcUtil.createQueriesToByte(message.getDhtMap().get("t").getBytes(), y,map), message.getNode()));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        downloadThreadPool.execute(new Runnable() {
//            @Override
//            public void run() {
//                //subMap.get("info_hash").getString();
//            }
//        });
        String infoHash = KrpcUtil.stringToHexString(target);
        logger.info("info_hash:{}", infoHash);
//        deduplication(subMap);
    }

    private void announcePeer(ChannelHandlerContext channelHandlerContext, final Map<String, BEValue> subMap, Message message) throws InvalidBEncodingException, CounterException {
        logger.info("announce_peer");
        int port = 0;
        if(subMap.containsKey("implied_port") && subMap.get("implied_port").getInt() != 0) {
            port = message.getNode().getPort();
        } else {
            port = subMap.get("port").getInt();
            if (port < 1 || port > 65535) {
                channelHandlerContext.close();
            }
        }
        counterService.increment(Constant.COUNTER_NMAE, CounterType.COUNT_ANNOUNCE);
        Map map = new HashMap<>();
        map.put("id", KrpcUtil.getNeighbor(message.getNode().getId()));
        try {
            channelHandlerContext.channel().write(new Message(KrpcUtil.createQueriesToByte(message.getDhtMap().get("t").getBytes(), "r",map), message.getNode()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        final int finalPort = port;
        findNodeThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("node:{}",message.getNode().toString());
                    logger.info("ip:{}",message.getNode().getIp());
                    logger.info("port:{}",finalPort);
                    storeClient.connect(message.getNode().getIp(), finalPort, subMap.get("info_hash").getBytes());
                } catch (InvalidBEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        deduplication(subMap);
    }

    private void findNodes(ChannelHandlerContext channelHandlerContext, Message message, byte [] target) throws CounterException {
        counterService.increment(Constant.COUNTER_NMAE, CounterType.COUNT_FIND_NODE);
        logger.info("findNodes queset -> {}:{}", message.getNode().getIp(), message.getNode().getPort());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("id", KrpcUtil.getNeighbor(message.getNode().getId()));
        map.put("nodes", "");
        try {
            channelHandlerContext.channel().write(new Message(KrpcUtil.createQueriesToByte(message.getDhtMap().get("t").getBytes(), "r",map), message.getNode()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onPing(ChannelHandlerContext channelHandlerContext, Message message) throws CounterException {
        counterService.increment(Constant.COUNTER_NMAE, CounterType.COUNT_PING);
        try {
            logger.info("ping queset -> {}:{}",message.getNode().getIp(), message.getNode().getPort());
            Map<String,BEValue> dhtMap = message.getDhtMap();
            Map<String,Object> map = new HashMap<String, Object>();
            map.put("id", KrpcUtil.getNeighbor(message.getNode().getId()));
            channelHandlerContext.channel().write(new Message(KrpcUtil.createQueriesToByte(dhtMap.get("t").getBytes(), "r", map), message.getNode()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //去重
    private synchronized void deduplication(Map<String, BEValue> subMap) throws InvalidBEncodingException {
        byte [] infoHashByte = subMap.get("info_hash").getBytes();
        String infoHash = KrpcUtil.stringToHexString(infoHashByte);
        logger.info("info_hash:{}", infoHash);
//        if(!Strings.isNullOrEmpty(infoHash)) {
//            if(!Constant.filter.mightContain(infoHash)) {
//                Constant.filter.put(infoHash);
//            }
//        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("error:{}", cause);
        super.exceptionCaught(ctx, cause);
    }
}
