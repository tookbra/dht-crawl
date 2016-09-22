package com.tookbra.dht.handler;

import com.tookbra.dht.Node;
import com.tookbra.dht.common.KrpcUtil;
import com.tookbra.dht.domian.Message;
import com.turn.ttorrent.bcodec.BEValue;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tookbra on 2016/8/4.
 */
public class DhtCodec extends MessageToMessageCodec<DatagramPacket,Message> {

    private static final Logger logger = LoggerFactory.getLogger(DhtCodec.class);

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, List<Object> list) throws Exception {
        //write

        Node node = message.getNode();
        //channelHandlerContext.write(new DatagramPacket(Unpooled.copiedBuffer(message.getBytes()),new InetSocketAddress(node.getIp(),node.getPort())));
//        list.add(new DatagramPacket(Unpooled.copiedBuffer(message.getBytes()),new InetSocketAddress(node.getIp(),node.getPort())));
        channelHandlerContext.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message.getBytes()),new InetSocketAddress(node.getIp(),node.getPort())));
//        channelHandlerContext.writeAndFlush(message);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket, List<Object> list) throws Exception {
        ByteBuf buf = datagramPacket.copy().content();
        if(buf == null) {
            return;
        }
        int num = buf.readableBytes();
        byte[] req = new byte[num];
        buf.readBytes(req);

        Map<String,BEValue> dict = KrpcUtil.dhtResp(req);
        if(!dict.isEmpty()) {
            if (Objects.isNull(dict.get("y"))) {
                logger.info("error message is error");
                return;
            } else if (dict.get("y").getBytes().equals("e")) {
                logger.error("error message is error");
                logger.error(dict.get("e").toString());
                return;
            }
            Node node = new Node(null, datagramPacket.sender().getHostString(), datagramPacket.sender().getPort());
            Message message = new Message(dict, node);
            ReferenceCountUtil.release(buf);
            list.add(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("{}",cause);
        ctx.fireExceptionCaught(cause);
    }
}
