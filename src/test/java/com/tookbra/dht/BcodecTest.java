package com.tookbra.dht;

import com.tookbra.dht.common.KrpcUtil;
import com.turn.ttorrent.bcodec.BEValue;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tookbra on 2016/8/15.
 */

public class BcodecTest {
    @Test
    public void test() throws IOException {
        String str = "d8:msg_typei1e5:piecei0e10:total_sizei49471eed6";
        int pos = str.indexOf("ee");
        System.out.println(pos);
        Map<String, BEValue> pieceMap = KrpcUtil.dhtResp(str.getBytes());
        System.out.println("1");
//        Map<String, Object> map = new HashMap<String, Object>();
//        Map<String, Object> map1 = new HashMap<String, Object>();
//        map1.put("ut_metadata",1);
//        map.put("m", map1);
//
//        System.out.printf(new String(KrpcUtil.enBencode(map)));

        /*Map<String, Object> subMap = new HashMap<String, Object>();
        subMap.put("id", Table.getId());
        subMap.put("target",  KrpcUtil.randomId());
        map.put("a", subMap);
*/
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        BEncoder.bencode(map,out);
//        byte[]b =out.toByteArray();
//        String s = new String(b, "UTF-8");
//        System.out.println(s);
//
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        BencodingOutputStream bencode = new BencodingOutputStream(stream);
//        bencode.writeMap(map);
//        System.out.println(stream.toString());
    }
}
