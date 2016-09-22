package com.tookbra.dht.redis.queue;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;

/**
 * @author tookbra
 * @date 2016/9/9
 */
public abstract class RedisQueue<T extends Serializable> implements Queue<T> {

    protected String queueName;

    @Autowired
    private RedisTemplate<Serializable, T> redisTemplate;

    private BoundListOperations<String, T> listOperations;

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public void setRedisTemplate(RedisTemplate<Serializable, T> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisQueue() {

    }
}
