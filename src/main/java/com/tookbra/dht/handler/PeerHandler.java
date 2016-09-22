package com.tookbra.dht.handler;

import com.tookbra.dht.common.KrpcUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by tookbra on 2016/8/3.
 */
public class PeerHandler extends ChannelDuplexHandler {

    private static final Logger logger = LoggerFactory.getLogger(PeerHandler.class);

    private byte [] infoHash;

    public PeerHandler() {
    }

    public PeerHandler(byte [] infoHash) {
        this.infoHash = infoHash;
    }

    private final String BT_PROTOCOL_NAME = "BitTorrent protocol";
    private final byte[] BT_RESERVED = new byte[] { (byte) (0x00 & 0xff),
            (byte) (0x00 & 0xff), (byte) (0x00 & 0xff), (byte) (0x00 & 0xff),
            (byte) (0x00 & 0xff), (byte) (0x10 & 0xff), (byte) (0x00 & 0xff),
            (byte) (0x01 & 0xff), };

    //建立连接后触发 第一次握手
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("send announce_peer");
        /***
         * 第一个字节：BT协议名称的长度
         第一个字节后面紧跟着BT协议名称的byte[]
         最后是固定48个字节，分为三部分：
         ① 8个保留字节，其中第6个字节填0x10，表示本地支持BT metadata协议，其他的字节是什么意思就不用管了。
         ② info_hash，20字节
         ③ peer_id，20字节（随机生成）
         */
        byte [] peerId = KrpcUtil.randomId();
        logger.info("BT_PROTOCOL_NAME:{}",BT_PROTOCOL_NAME.length());
        logger.info("BT_PROTOCOL_NAME:{}",BT_PROTOCOL_NAME.getBytes().length);
        logger.info("BT_RESERVED:{}",BT_RESERVED.length);
        logger.info("infoHash:{}",infoHash.length);
        logger.info("peerId:{}",peerId.length);
        //第一个字节为 协议名称的字节长度
        ByteBuf buffer = Unpooled.buffer(BT_PROTOCOL_NAME.getBytes().length+BT_PROTOCOL_NAME.getBytes().length+16+infoHash.length+peerId.length);
        buffer.writeByte(BT_PROTOCOL_NAME.getBytes().length);
        buffer.writeBytes(BT_PROTOCOL_NAME.getBytes());
        buffer.writeBytes(BT_RESERVED);
//        buffer.writeInt(16);
        buffer.writeBytes(infoHash);
        buffer.writeBytes(peerId);
        ctx.channel().writeAndFlush(buffer);

        super.channelActive(ctx);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("connection closed . {}" );
    }

//    @Override
//    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
//
//    }
}
