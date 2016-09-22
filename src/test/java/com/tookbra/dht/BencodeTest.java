package com.tookbra.dht;

import com.tookbra.dht.common.KrpcUtil;
import com.turn.ttorrent.bcodec.BEncoder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by tookbra on 2016/7/25.
 */
public class BencodeTest {

    @Test
    public void testFindNode() throws IOException, NoSuchAlgorithmException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("t", KrpcUtil.getRandomString(2));
        map.put("y", "q");
        map.put("q", "find_node");

        Map<String, Object> subMap = new HashMap<String, Object>();
        subMap.put("id", Table.getId());
        subMap.put("target", KrpcUtil.randomId());
        map.put("a", subMap);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BEncoder.bencode(map, out);
        byte[]b =out.toByteArray();
        String s = new String(b, "UTF-8");
        System.out.println(s);
    }
}
