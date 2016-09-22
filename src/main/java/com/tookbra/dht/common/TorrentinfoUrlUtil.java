package com.tookbra.dht.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author tookbra
 * @date 2016/8/30
 */
public class TorrentinfoUrlUtil {
    public  static  final String [] urls = new String []{
            "http://www.torrent.org.cn/Home/Torrent/download?hash=%1$s.torrent",
            "http://bt.box.n0808.com/%1$s/%2$s/%3$s.torrent",
            "http://torrent-cache.bitcomet.org:36869/get_torrent?info_hash=%1$s&key=%2$s",
            "http://torcache.net/torrent/%1$s.torrent",
            "http://www.sobt.org/Tool/downbt?info=%1$s",
    };

    public static String formatUrl(String info_hash,Integer time){
        String url = "";
        switch(time) {
            case 1:		//迅雷
                url = String.format(urls[time], info_hash.substring(0, 2), info_hash.substring(info_hash.length() - 2, info_hash.length()), info_hash);
                break;
            case 2://bitcomet
                url=String.format(urls[time], info_hash,calcKey(info_hash));
                break;
            default:
                url = String.format(urls[time], info_hash);
                break;
        }

        return url;
    }

    private static String calcKey(String infoHashHex){
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
            md.update("bc".getBytes());
            md.update(KrpcUtil.HexString2Bytes(infoHashHex));
            md.update("torrent".getBytes());
            return KrpcUtil.bytesToHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String [] args) {
        System.out.println(formatUrl("8761E9485D810059BDD07BCCC1A635AA8212497B",2));
    }
}
