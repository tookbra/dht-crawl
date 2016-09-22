package com.tookbra.dht.handler;

import com.tookbra.dht.common.KrpcUtil;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.*;


/**
 * Created by tookbra on 2016/8/3.
 */
public class MetadataHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private static final Logger logger = LoggerFactory.getLogger(MetadataHandler.class);

    private final String BT_PROTOCOL_NAME = "BitTorrent protocol";
    private final byte[] BT_RESERVED = new byte[] { (byte) (0x00 & 0xff),
            (byte) (0x00 & 0xff), (byte) (0x00 & 0xff), (byte) (0x00 & 0xff),
            (byte) (0x00 & 0xff), (byte) (0x10 & 0xff), (byte) (0x00 & 0xff),
            (byte) (0x01 & 0xff), };

    private final byte BT_MSG_ID = 20 & 0xff;
    private final int EXT_HANDSHAKE_ID = 0;
    private final int MAX_METADATA_SIZE = 1000000;
    private byte [] metadata;
    private boolean [] finished;
    private byte [] infoHash;
    private int metadata_size;
    private int ut_metadata;
    private Map<String, Object> map = new HashMap<>();

    List<byte[]> list = new ArrayList<>();
    int count = 0;

    private boolean isHandshakeOK = false;

    public MetadataHandler() {
    }

    public MetadataHandler(byte [] infoHash) {
        this.infoHash = infoHash;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        InetSocketAddress insocket = (InetSocketAddress) channelHandlerContext.channel().remoteAddress();
        logger.info("isHandshakeOK:{}", isHandshakeOK);

        if(!isHandshakeOK) {
            byte proLen = byteBuf.readByte();
            if(proLen == 0 ) {
                channelHandlerContext.fireChannelInactive();
            }
            String protocol = byteBuf.readBytes(19).toString(Charset.defaultCharset());
            logger.info("protocol:{}", protocol);
            if (!BT_PROTOCOL_NAME.equals(protocol)) {
                logger.error("handshake failed.");
                channelHandlerContext.fireChannelInactive();
            }

            byte[] contentbytes = new byte[8];
            byteBuf.readBytes(contentbytes);
            if (contentbytes[5] == 16) {
                byte[] infoHash1 = new byte[20];
                byteBuf.readBytes(infoHash1);
                if (!Arrays.equals(infoHash1, infoHash)) {
                    logger.error("remote peer don't support download metadata.");
                } else {
                    byte[] peerId = new byte[20];
                    byteBuf.readBytes(peerId);
                    isHandshakeOK = true;
                    send(channelHandlerContext, EXT_HANDSHAKE_ID, KrpcUtil.utMetadata());
                }
            } else {
                logger.error("remote peer don't support download metadata.");
                channelHandlerContext.fireChannelInactive();
            }
        } else {
            long length = byteBuf.readUnsignedInt();
            if(length > 0) {
                byte idMessage = byteBuf.readByte();
                logger.info("idMessage:{}",idMessage);
                if(BT_MSG_ID == idMessage) {
                    int extendedID = byteBuf.readByte();
                    byte[] dst = new byte[byteBuf.readableBytes()];
                    byteBuf.readBytes(dst);
                    list.add(dst);
                    if (extendedID == 0) {
                        if (dst.length == 0) {
                            return;
                        }
                        Map<String, BEValue> metadataMap = KrpcUtil.dhtResp(dst);
                        if (metadataMap.containsKey("m")) {
                            Map<String, BEValue> m = metadataMap.get("m").getMap();
                            if (m == null || !m.containsKey("ut_metadata") || !metadataMap.containsKey("metadata_size")) {
                                logger.error("onExtendHandShake failed");
                                channelHandlerContext.fireChannelInactive();
                            }
                            this.ut_metadata = m.get("ut_metadata").getInt();
                            this.metadata_size = metadataMap.get("metadata_size").getInt();
                            if (metadata_size > MAX_METADATA_SIZE) {
                                channelHandlerContext.fireChannelInactive();
                            }
                            logger.info("metadata_size:{}", metadata_size);
                            metadata = new byte[this.metadata_size];
                            int num_piece = (int) Math.ceil(this.metadata_size / (16.0 * 1024));
                            finished = new boolean[num_piece];
                            for (int piece = 0; piece < num_piece; piece++) {
                                map.clear();
                                map.put("msg_type", 0);
                                map.put("piece", piece);
                                logger.info("send piece 1");
                                send(channelHandlerContext, this.ut_metadata, KrpcUtil.enBencode(map));
                            }
                        }
                    } else {
                        for(byte [] b : list) {
                            count += b.length;
                        }
                        byte [] b = new byte[count];
                        int destLen = 0;
                        for(byte [] b1 : list) {
                            System.arraycopy(b1, 0, b, destLen, b1.length);
                            destLen += b1.length;
                        }
                        String str = new String(b);
                        logger.info("str:{}", str);
                        int pos = str.indexOf(":total_size");
                        Map<String, BEValue> pieceMap = KrpcUtil.dhtResp(b);
                        logger.info("pieceMap:{}", pieceMap.toString());
                        byte[] piece_metadata = Arrays.copyOfRange(b, pos, b.length);

                        if (!pieceMap.containsKey("msg_type") || !pieceMap.containsKey("piece")) {
                            logger.error("onPiece packet error.");
                            return;
                        }

                        if (pieceMap.get("msg_type").getInt() != 1) {
                            logger.error("onPiece error, msg_type:" + map.get("msg_type").toString());
                            return;
                        }

                        int piece = pieceMap.get("piece").getInt();
                        System.arraycopy(piece_metadata, 0, this.metadata, piece * 16 * 1024, piece_metadata.length);
                        finished[piece] = true;
                        boolean b1 = true;
                        for (int i = 0; i < finished.length; i++) {
                            if (!finished[i]) {
                                b1 = false;
                                break;
                            }
                        }
                        logger.info("b1:{}", b1);
                        if (b1) {
                            logger.info("metadata_size:{}", metadata_size);
                            Map<String, BEValue> map = KrpcUtil.dhtResp(Arrays.copyOfRange(metadata, 0, metadata_size));
                            logger.info("map:{}", map);
                            Map<String, BEValue> info;
                            if (map != null && map.size() > 0) {
                                if (map.containsKey("info")) {
                                    info = map.get("info").getMap();
                                } else {
                                    info = map;
                                }

                                logger.info("info:{}", info.toString());

                                if (!info.containsKey("name")) {
                                    logger.info("name is empty");
                                }


                                if (map.containsKey("encoding")) {
                                    String encoding = map.get("encoding").getString();
                                    logger.info("info name:{}", info.get("name").getString(encoding));
                                } else {
                                    logger.info("info name:{}", info.get("name").getString());
                                }

                                if (info.containsKey("files")) {
                                    List<BEValue> list = info.get("files").getList();
                                    Long countLength = new Long(0);
                                    for (BEValue beValue : list) {
                                        Map<String, BEValue> fileMap = beValue.getMap();
                                        Long fileLength = fileMap.get("length").getLong();
                                        countLength += fileLength;
                                    }
                                    logger.info("length:{}", length);

                                }
                            }
                        }
                    }
                }
//                else {
//                   logger.warn("error");
//                }

            } else {
                logger.info("length == 0");
                channelHandlerContext.channel().writeAndFlush(new byte[]{0,0,0,0});
            }
        }
    }


    //第一次握手
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
//        logger.info("BT_PROTOCOL_NAME:{}",BT_PROTOCOL_NAME.length());
//        logger.info("BT_PROTOCOL_NAME:{}",BT_PROTOCOL_NAME.getBytes().length);
//        logger.info("BT_RESERVED:{}",BT_RESERVED.length);
//        logger.info("infoHash:{}",infoHash.length);
//        logger.info("peerId:{}",peerId.length);
        //第一个字节为 协议名称的字节长度
        ByteBuf buffer = Unpooled.buffer(BT_PROTOCOL_NAME.getBytes().length+BT_PROTOCOL_NAME.getBytes().length+16+infoHash.length+peerId.length);
        buffer.writeByte(BT_PROTOCOL_NAME.getBytes().length);
        buffer.writeBytes(BT_PROTOCOL_NAME.getBytes());
        buffer.writeBytes(BT_RESERVED);
        buffer.writeBytes(infoHash);
        buffer.writeBytes(peerId);
        logger.info("seq 1");
        ctx.channel().writeAndFlush(buffer);


    }

    private void send(ChannelHandlerContext ctx, int id, byte[] data) {
        byte[] length_prefix = intToByteArray(data.length + 2);
        for(int i=0; i<4; i++)
            length_prefix[i] = (byte)(length_prefix[i] & 0xff);

        ByteBuf buffer = Unpooled.buffer(BT_MSG_ID + id & 0xff+ length_prefix.length+data.length);

        buffer.writeBytes(length_prefix);
        //bittorrent message ID, = 20
        buffer.writeByte(BT_MSG_ID);
        //extended message ID. 0 = handshake, >0 = extended message as specified by the handshake.
        buffer.writeByte(id & 0xff);
        buffer.writeBytes(data);
        ctx.channel().writeAndFlush(buffer);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn("connection closed . {}" );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    public static byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }
}
