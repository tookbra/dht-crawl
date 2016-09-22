package com.tookbra.dht;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import io.netty.util.AttributeKey;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by tookbra on 2016/7/27.
 */
public class Constant {
    /** Node ID最小值 */
    public final static String NODE_ID_MIN = "0";

    /** Node ID最大值 */
    public final static String NODE_ID_MAX = "1461501637330902918203684832716283019655932542975";

    /** Bucket内Node空间大小 */
    public final static Integer BUCKET_NODE_SPACE = 8;

    /** Node数据传输大小 */
    public final static Integer NODE_INFO_LENGTH_ON_DHT = 26;

    /** Node信息中ID末值索引 */
    public final static Integer NODE_INFO_ID_LAST_INDEX = 19;

    /** Node信息中IP值索引 */
    public final static Integer NODE_INFO_IP_START_INDEX = 20;

    /** Node信息中IP末值索引 */
    public final static Integer NODE_INFO_IP_LAST_INDEX = 23;

    /** Node信息中PORT值索引 */
    public final static Integer NODE_INFO_PORT_START_INDEX = 24;

    /** Node信息中PORT末值索引 */
    public final static Integer NODE_INFO_PORT_LAST_INDEX = 25;

    public final static int ACT_BITFIELD = 5;
    public final static int ACT_CANCEL = 8;
    public final static int ACT_CHOKE = 0;
    public final static int ACT_HAVE = 4;
    public final static int ACT_INTERESTED = 2;
    public final static int ACT_NOT_INTERESTED = 3;
    public final static int ACT_PIECE = 7;
    public final static int ACT_PORT = 9;
    public final static int ACT_REQUEST = 6;
    public final static int ACT_UNCHOKE = 1;
    public final static int ACT_KEEP_ALIVE = -1;

    public static final BlockingQueue<Node> queue = new ArrayBlockingQueue(10000);
    public static final ConcurrentLinkedQueue<Node> concurrentLinkedQueue = new ConcurrentLinkedQueue();

    public static  AttributeKey<InetSocketAddress> TARGET_ADDRESS = AttributeKey.newInstance("TARGET_ADDRESS");
    public static final BloomFilter<CharSequence> filter = BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 10000000, 0.001F);

    public final static String COUNTER_NMAE = "DHT";

    public static long FinderDelayTime = 1000;
}
