package com.tookbra.dht.bootstrap;

import com.tookbra.dht.Monitor;
import com.tookbra.dht.handler.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tookbra on 2016/7/23.
 */
public class DhtClient {

    public Bootstrap bootstrap = new Bootstrap();

    public static Channel channel;

    private  final static EventLoopGroup workgroup = new NioEventLoopGroup(0, newThreadFactory("workGroup"));

    @Autowired
    private DhtHandler dhtHandler;

    @Autowired
    private FindNodeHandler findNodeHandler;

    public void run() throws InterruptedException, NoSuchAlgorithmException, IOException {
        bootstrap.group(workgroup)
                .channel(NioDatagramChannel.class)//UDP通道
                .option(ChannelOption.SO_RCVBUF, 65536)
//                .option(ChannelOption.SO_SNDBUF, 268435456)
                .option(ChannelOption.SO_SNDBUF, 65536)
                .handler(initPipeLine());
        ChannelFuture future = bootstrap.bind(6881).sync();
        channel = future.channel();
//        channel.closeFuture().await();



    }

    public ChannelHandler initPipeLine() {
        return new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
                ChannelPipeline channelPipeline = nioDatagramChannel.pipeline();
                channelPipeline.addLast("Logger", new LoggingHandler(LogLevel.DEBUG));
                channelPipeline.addLast("codec", new DhtCodec());
//                channelPipeline.addLast("test", new Test3());
                channelPipeline.addLast("findNodeHandler", findNodeHandler);
                channelPipeline.addLast("dhtHandler",dhtHandler);
                channelPipeline.addLast("BlackHoleHandler",new BlackHoleHandler());
            }
        };

    }


    public static void main(String [] args) throws InterruptedException, IOException, NoSuchAlgorithmException {
        DhtClient dhtClient = new DhtClient();
        dhtClient.run();
    }

    private static ThreadFactory newThreadFactory(final String name){

        return new ThreadFactory() {

            private final AtomicInteger threadNumber = new AtomicInteger(1);

            public Thread newThread(Runnable r) {
                Thread t = new Thread( r,name + threadNumber.getAndIncrement());

                t.setDaemon(true);
                if (t.getPriority() != Thread.NORM_PRIORITY)
                    t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        };

    }


}
