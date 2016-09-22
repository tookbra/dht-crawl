package com.tookbra.dht.redis.modal;

/**
 * @author tookbra
 * @date 2016/9/8
 */
public enum CounterType {
    COUNT_FIND_REQUEST("countFindRequest","发出find_node请求数量"),
    COUNT_FIND_RESPONSE("countFindResponse","收到find_node回复数量"),
    COUNT_FIND_NODE("countFindNode","收到find_node请求数量"),
    COUNT_PING("countPing","收到ping请求数量"),
    COUNT_ANNOUNCE("countAnnounce","收到announce_peer请求数量"),
    COUNT_GET_PEERS("countGetPeers","收到get_peers请求数量"),
    PRE_COUNT_GET_PEERS("preCountGetPeers","收到pre_count_peers请求数量"),;

    private String type;
    private String description;

    CounterType(String type, String description) {
        this.type = type;
        this.description = description;
    }

    public String getType(){
        return this.type;
    }

    public String getDescription(){
        return this.description;
    }

    public static CounterType getCounterType(String name){
        for (CounterType counterType : CounterType.values()) {
            if(counterType.toString().equals(name)){
                return counterType;
            }
        }
        return null;
    }
}
