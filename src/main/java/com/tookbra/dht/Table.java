package com.tookbra.dht;

import com.tookbra.dht.common.KrpcUtil;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by tookbra on 2016/7/27.
 */
public class Table {
    /** 路由表的id */
    private static byte[] id;

    /** 路由表内的桶 */
    private static List<Bucket> buckets;

    /** 桶最大最小值列表 */
    private static List<BigDecimal> bucketsFlagList;

    static  {
        id = KrpcUtil.randomId();
        buckets = new ArrayList<Bucket>();
        bucketsFlagList = new ArrayList<BigDecimal>();
        Bucket bucket = new Bucket(new BigDecimal(Constant.NODE_ID_MIN), new BigDecimal(Constant.NODE_ID_MAX));
        buckets.add(bucket);
    }

    private Table()
    {

    }

    public synchronized static void appendNode(Node node) {
        if (Arrays.equals(id, node.getId())) {
            return;
        }
        int index = getBucketIndex(node.getId());
        Bucket bucket = buckets.get(index);
        try {
            bucket.appendNode(node);
        }
        catch (Exception e) {
            if (!bucket.nodeIdInRange(node.getId())) {
                return;
            }
            spilt_bucket(index);
            appendNode(node);
        }
    }

    public static List<Node> findCloseNodes(byte[] targetId) {
        List<Node> nodes = new ArrayList<Node>();
        if (buckets.size() == 0) {
            return nodes;
        }
        int index = getBucketIndex(targetId);
        nodes = buckets.get(index).getNodes();
        int smallBucketIndex = index - 1;
        int bigBucketIndex = index + 1;
        while (nodes.size() < Constant.BUCKET_NODE_SPACE && (smallBucketIndex > 0 || bigBucketIndex < buckets.size())) {
            if (smallBucketIndex > 0) {
                nodes = KrpcUtil.extendArray(nodes, buckets.get(smallBucketIndex).getNodes());
            }
            if (bigBucketIndex <= buckets.size())
            {
                nodes = KrpcUtil.extendArray(nodes, buckets.get(bigBucketIndex).getNodes());
            }
            smallBucketIndex--;
            bigBucketIndex++;
        }
        Collections.sort(nodes);
        return nodes;
    }

    private static void spilt_bucket(int index)
    {
        Bucket oldBucket = buckets.get(index);
        BigDecimal point = oldBucket.getMax().subtract((oldBucket.getMax().subtract(oldBucket.getMin())).divide(new BigDecimal(2))).setScale(0, BigDecimal.ROUND_DOWN);
        Bucket newBucket = new Bucket(new BigDecimal(point.toString()).add(new BigDecimal("1")),oldBucket.getMax());
        oldBucket.setMax(new BigDecimal(point.toString()));
        for (Node node : oldBucket.getNodes())
        {
            if (newBucket.nodeIdInRange(node.getId()))
            {
                newBucket.getNodes().add(node);
            }
        }
        for (Node node : newBucket.getNodes())
        {
            oldBucket.getNodes().remove(node);
        }
        buckets.add(index, newBucket);
    }

    private static int getBucketIndex(byte[] id) {
        for (Bucket bucket : buckets) {
            if (bucket.nodeIdInRange(id)) {
                return buckets.indexOf(bucket);
            }
        }
        return buckets.size() - 1;
    }

    public static byte[] getId() {
        return id;
    }

    public static void setId(byte[] id) {
        Table.id = id;
    }

    public static List<Bucket> getBuckets() {
        return buckets;
    }

    public static void setBuckets(List<Bucket> buckets) {
        Table.buckets = buckets;
    }

    public static List<BigDecimal> getBucketsFlagList() {
        return bucketsFlagList;
    }

    public static void setBucketsFlagList(List<BigDecimal> bucketsFlagList) {
        Table.bucketsFlagList = bucketsFlagList;
    }
}
