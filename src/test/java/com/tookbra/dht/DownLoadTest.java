package com.tookbra.dht;

import com.tookbra.dht.common.DateUtil;
import com.tookbra.dht.common.TorrentinfoUrlUtil;
import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.BEncoder;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author tookbra
 * @date 2016/8/30
 */
public class DownLoadTest {

    public static void main(String [] args) {
        String url = TorrentinfoUrlUtil.formatUrl("9FED5DF63643243379CC1030E39CBC253204A64C",0);
        try {
            HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
            con.setConnectTimeout( 10 * 1000);
            con.setReadTimeout( 10* 1000);
            con.setUseCaches(false);
            con.setDoInput(true);
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent","Mozilla/5.0");

            int responseCode = con.getResponseCode();
            BufferedInputStream bis = null;
            if (responseCode ==HttpURLConnection.HTTP_OK) {
                BEValue beValue = BDecoder.bdecode(con.getInputStream());
                Map<String, BEValue> map = beValue.getMap();
//
                if (map.containsKey("creation date")) {
                    Date date = new Date(map.get("creation date").getLong() * 1000);
                    System.out.println("create time:" + date);
                }
                if (map.containsKey("info")) {
                    Map<String, BEValue> infoMap = map.get("info").getMap();
                    if (infoMap.containsKey("name")) {
                        System.out.println("name:"+new String(infoMap.get("name").getBytes(), "UTF-8"));
                    }
                }
                System.out.println(beValue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
