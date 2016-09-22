package com.tookbra.dht.redis.service.impl;

import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * @author tookbra
 * @date 2016/9/8
 */
@Service
public class RedisServiceImpl<T extends Serializable> {

    @Resource
    private RedisTemplate<Serializable, Serializable> redisTemplate;

    private HashOperations<Serializable, Object, Object> hashOps = null;

    private SetOperations<Serializable, T> setOps = null;

    private ListOperations<Serializable, T> listOps = null;

    private BoundListOperations<Serializable, T> bListOps = null;

    /**
     *
     * @param key
     * @param field
     * @param delta
     */
    public void hincreby(String key,Object field,Integer delta){
        hashOps = redisTemplate.opsForHash();
        hashOps.increment(key,field,delta);
    }

    /**
     *
     * @param channel
     * @param message
     */
    public void sendMessage(String channel, Serializable message) {

        redisTemplate.convertAndSend(channel, message);
    }

    /**
     * 入列
     * @param key
     * @param message
     */
    public void listLpush(String key, Serializable message) {
        redisTemplate.boundListOps(key).rightPush(message);
    }

    /**
     * 出列
     * @param key
     */
    public Serializable leftPop(String key) {
        return redisTemplate.boundListOps(key).rightPop();
    }

    public Serializable leftPop(String key, Long time, TimeUnit timeUnit) {

        return redisTemplate.opsForList().leftPop(key, time, timeUnit);
    }

    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

}
