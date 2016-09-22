package com.tookbra.dht.redis.service;

import com.tookbra.dht.exception.CounterException;
import com.tookbra.dht.redis.modal.CounterType;

/**
 * @author tookbra
 * @date 2016/9/8
 */
public interface CounterService {
    /**
     *
     * @param key
     * @param counterType
     * @throws CounterException
     */
    void increment(String key, CounterType counterType) throws CounterException;

    /**
     *
     * @param key
     * @param counterType
     * @param num
     * @throws CounterException
     */
    void increment(String key, CounterType counterType, Integer num) throws CounterException;

    /**
     *
     * @param key
     * @param field
     * @param num
     * @throws CounterException
     */
    void incrementCount(String key, String field, Integer num) throws CounterException;
}
