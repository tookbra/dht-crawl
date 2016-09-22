package com.tookbra.dht.domian;

import com.tookbra.dht.Node;
import com.turn.ttorrent.bcodec.BEValue;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Created by tookbra on 2016/8/4.
 */
public class Message implements Serializable {

    private byte [] bytes;
    private Map<String, BEValue> dhtMap;
    private Node node;

    public Message() {
    }

    public Message(byte[] bytes, Node node) {
        this.bytes = bytes;
        this.node = node;
    }

    public Message(Map<String, BEValue> dhtMap, Node node) {
        this.dhtMap = dhtMap;
        this.node = node;
    }


    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public Map<String, BEValue> getDhtMap() {
        return dhtMap;
    }

    public void setDhtMap(Map<String, BEValue> dhtMap) {
        this.dhtMap = dhtMap;
    }
}
