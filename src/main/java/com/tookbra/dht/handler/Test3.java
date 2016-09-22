package com.tookbra.dht.handler;

import com.tookbra.dht.Node;
import com.tookbra.dht.domian.Message;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.DatagramPacket;

import java.net.InetSocketAddress;

/**
 * Created by tookbra on 2016/8/3.
 */
public class Test3 extends ChannelDuplexHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof Message) {
            Message message = (Message)msg;
            Node node = message.getNode();
            ctx.write(new DatagramPacket(Unpooled.copiedBuffer(message.getBytes()),new InetSocketAddress(node.getIp(),node.getPort())), promise);
        }

    }
}
