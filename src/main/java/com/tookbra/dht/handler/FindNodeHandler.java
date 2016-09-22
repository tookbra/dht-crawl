package com.tookbra.dht.handler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.tookbra.dht.Constant;
import com.tookbra.dht.Node;
import com.tookbra.dht.Table;
import com.tookbra.dht.bootstrap.DhtClient;
import com.tookbra.dht.common.KrpcUtil;
import com.tookbra.dht.domian.Message;
import com.tookbra.dht.exception.CounterException;
import com.tookbra.dht.redis.modal.CounterType;
import com.tookbra.dht.redis.service.CounterService;
import com.tookbra.dht.redis.service.impl.RedisServiceImpl;
import com.tookbra.dht.task.FindNodeTask;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Objects;


/**
 * Created by tookbra on 2016/7/26.
 */
@ChannelHandler.Sharable
@Component
public class FindNodeHandler extends SimpleChannelInboundHandler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(FindNodeHandler.class);

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
        if (dhtMap.get("y").getString().equals("r")) {
            Map<String, BEValue> subMap =  dhtMap.get("r").getMap();
            if (subMap.containsKey("nodes")) {
                onFindNodesResponse(channelHandlerContext,dhtMap);
            }
            channelHandlerContext.fireChannelInactive();
        }
        channelHandlerContext.fireChannelRead(message);
    }

    private void onFindNodesResponse(ChannelHandlerContext channelHandlerContext,Map<String, BEValue> dhtMap) throws Exception {
        Map<String, BEValue> subMap = dhtMap.get("r").getMap();
        if (subMap.containsKey("nodes")) {
            List<Node> result = KrpcUtil.decodeNodes(subMap.get("nodes").getBytes());
            result = ImmutableSet.copyOf(result).asList();
            result.stream().forEach(node ->{
                try {
                    channelHandlerContext.channel().writeAndFlush(new Message(KrpcUtil.findNode(node.getId()), node));
                    counterService.increment(Constant.COUNTER_NMAE, CounterType.COUNT_FIND_RESPONSE);
                } catch (CounterException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                redisService.listLpush("node", node);
            });
        }
    }
}
