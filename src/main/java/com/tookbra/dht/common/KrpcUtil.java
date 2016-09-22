package com.tookbra.dht.common;

import com.google.common.collect.Lists;
import com.tookbra.dht.Constant;
import com.tookbra.dht.Node;
import com.tookbra.dht.Table;
import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Created by tookbra on 2016/7/28.
 */
public class KrpcUtil {

    private static final Logger logger = LoggerFactory.getLogger(KrpcUtil.class);

    private final static Random random = new Random();

    final static char[] digits = {
            '0' , '1' , '2' , '3' , '4' , '5' ,
            '6' , '7' , '8' , '9' , 'a' , 'b' ,
            'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
            'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
            'o' , 'p' , 'q' , 'r' , 's' , 't' ,
            'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static String getRandomString(int size) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++) {
            int randomNum = random.nextInt(256);
            sb.append(Character.toChars(randomNum));
        }

        return sb.toString();
    }

    public static byte[] randomId(){
        byte[] bytes = new byte[20];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte)random.nextInt(256);
        }
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        messageDigest.update(bytes);
        return messageDigest.digest();
    }

    public static byte [] findNode(byte [] nodeId) throws IOException, NoSuchAlgorithmException {
        byte [] nid = Objects.isNull(nodeId) ? Table.getId() : getNeighbor(nodeId);
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("t", getRandomString(2));
        map.put("y", "q");
        map.put("q", "find_node");

        Map<String, Object> subMap = new HashMap<String, Object>();
        subMap.put("id", nid);
        subMap.put("target", randomId());
        map.put("a", subMap);
        return enBencode(map);
    }



    /**
     * 解析dht请求回复
     * @return
     * @throws IOException
     */
    public static Map<String,BEValue> dhtResp(byte[] req) throws IOException {

        ByteArrayInputStream in = new ByteArrayInputStream(req);

        try {
            BEValue beValue = BDecoder.bdecode(in);
            Map<String, BEValue> dict = beValue.getMap();
            return dict;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    public static List<Node> decodeNodes(byte [] nodes) throws UnknownHostException {
        List<Node> result = Lists.newArrayList();
        int size = nodes.length;

        if (size % Constant.NODE_INFO_LENGTH_ON_DHT != 0) {
            return result;
        }

        for (int i = 0; i < size; i += 26) {
            byte[] currentNodeId = getByteArray(nodes, i, i + Constant.NODE_INFO_ID_LAST_INDEX);
            byte[] currentNodeIp = getByteArray(nodes, i + Constant.NODE_INFO_IP_START_INDEX, i + Constant.NODE_INFO_IP_LAST_INDEX);
            byte[] currentNodePort = getByteArray(nodes, i + Constant.NODE_INFO_PORT_START_INDEX, i + Constant.NODE_INFO_PORT_LAST_INDEX);

            int port = getPort(currentNodePort);
            String ip = InetAddress.getByAddress(currentNodeIp).getHostAddress();
            Node node = new Node(currentNodeId, ip, port);
            result.add(node);
        }

        return result;
    }

    public static byte[] getByteArray(byte[] bytes, int start, int end) {
        byte[] newByteArray = new byte[end - start + 1];

        for(int i = start; i <= end; i ++) {
            newByteArray[i-start] = bytes[i];
        }

        return newByteArray;
    }

    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] HexString2Bytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

    public static String stringToHexString(String src){
        byte[] bytes = src.getBytes(Charset.defaultCharset());
        return bytesToHexString(bytes);
    }

    public static String stringToHexString(byte [] src){
        return bytesToHexString(src);
    }

    /**
     * 创建请求数据包
     *
     * @param t   	transaction id
     * @param y   	数据包类型：查询(q)、答复(r)
     * @param arg	内容
     * @return map
     */
    private static Map<String, Object> createQueries(byte[] t, String y, Map<String, Object> arg) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("t", t);
        map.put("y", y);
        if (!arg.containsKey("id"))
            arg.put("id", Table.getId());

        if (y.equals("q")) {
            map.put("q", t);
            map.put("a", arg);
        } else {
            map.put("r", arg);
        }
        return map;
    }

    public static byte [] createQueriesToByte(byte[] t, String y, Map<String, Object> arg) throws IOException {
       return  KrpcUtil.enBencode(KrpcUtil.createQueries(t, y, arg));
    }

    public static Map<String, Object> findNode(byte[] nid, byte[] target) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("target", new String(target));
        if (nid != null)
            map.put("id", new String(getNeighbor(nid)));
        return map;
    }



    public static byte[] getNeighbor(byte[] info_hash) {
        byte[] bytes = new byte[20];
        System.arraycopy(info_hash, 0, bytes, 0, 19);
        System.arraycopy(Table.getId(), 1, bytes, 19, 1);
        return bytes;
    }

    public static byte[] enBencode(Map<String, Object> map) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BEncoder.bencode(map, out);
        return out.toByteArray();
    }

    public static <T> List<T> extendArray(List<T> list1, List<T> list2) {
        List<T> result = new ArrayList<T>();
        for (T t: list1)
        {
            result.add(t);
        }
        for (T t : list2)
        {
            result.add(t);
        }
        return result;
    }

    public static <T> ArrayList<T> removeSameInArray(List<T> list) {
        return new ArrayList<T>(new LinkedHashSet<T>(list));
    }


    public static String toUnsignedString(BigDecimal bigDecimal, int shift) {
        BigDecimal divisor = new BigDecimal(shift);
        Deque<Character> numberDeque = new ArrayDeque<Character>();
        do {
            BigDecimal[] ba = bigDecimal.divideAndRemainder(divisor);
            bigDecimal = ba[0];
            numberDeque.addFirst(digits[ba[1].intValue()]);
        } while (bigDecimal.compareTo(BigDecimal.ZERO) > 0);
        StringBuilder builder = new StringBuilder();
        for (Character character : numberDeque) {
            builder.append(character);
        }
        return builder.toString();
    }


    public static byte [] utMetadata() throws IOException {
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("ut_metadata",1);
        map.put("m", map1);

        return KrpcUtil.enBencode(map);
    }

    public static int getPort(byte[] bytes) {
        return ((bytes[0] << 8) & 0x0000ff00) | (bytes[1] & 0x000000ff);
    }
}
