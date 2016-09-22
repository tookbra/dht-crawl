package com.tookbra.dht.domian;

import java.io.Serializable;

/**
 * Created by tookbra on 2016/9/13.
 */
public class TorrentFile implements Serializable {

    private Long id;

    private Long torrentId;

    private String name;

    private Integer length;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTorrentId() {
        return torrentId;
    }

    public void setTorrentId(Long torrentId) {
        this.torrentId = torrentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}
