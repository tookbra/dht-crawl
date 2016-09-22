package com.tookbra.dht.bootstrap;

import com.tookbra.dht.handler.MetadataHandler;
import com.tookbra.dht.handler.PeerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


/**
 * Created by tookbra on 2016/8/16.
 */
@Component
public class StoreClient {

    private static final Logger logger = LoggerFactory.getLogger(StoreClient.class);

    private EventLoopGroup workLoopGroup;

    private byte [] infoHash;

    public void connect(String ip, Integer port, byte [] info_hash) {
        this.infoHash = info_hash;
        workLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_RCVBUF, 4096)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                .handler(initPipeLine());

        try {
            ChannelFuture f = bootstrap.connect(ip, port).sync();
            f.addListener(new ChannelFutureListener() {

                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        System.out.println("client connected");
                    } else {
                        System.out.println("server attemp failed");
                        future.cause().printStackTrace();
                    }

                }
            });
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                workLoopGroup.shutdownGracefully().sync();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    public ChannelHandler initPipeLine() {
        return new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("Logger1", new LoggingHandler(LogLevel.DEBUG));
//                pipeline.addLast("FrameDecoder", new LineBasedFrameDecoder(65533));
//                pipeline.addLast("ReadTimeoutHandler", new ReadTimeoutHandler(60));
//                pipeline.addLast("IdleStateHandler", new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
                pipeline.addLast("handler", new MetadataHandler(infoHash));
//                pipeline.addLast("connectManager", new PeerHandler());
            }
        };
    }

    public static void main(String [] args) {
        byte [] node  = new byte[]{10, 58, 26, -21, -77, -90, -37, 60, -121, 12, 62, -103, 36, 94, 13, 28, 6, -73, 71, -69};
        StoreClient storeClient = new StoreClient();
        storeClient.connect("41.105.149.92",34322,node);

    }
}
