package com.tookbra.dht.redis.service.impl;

import com.tookbra.dht.exception.CounterException;
import com.tookbra.dht.redis.modal.CounterType;
import com.tookbra.dht.redis.service.CounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author tookbra
 * @date 2016/9/8
 */
@Service
public class CounterServiceImpl implements CounterService {

    @Autowired
    private RedisServiceImpl redisService;

    @Override
    public void increment(String key, CounterType counterType) throws CounterException {
        this.incrementCount(key, counterType.getType(), 1);
    }

    @Override
    public void increment(String key, CounterType counterType, Integer num) throws CounterException {
        this.incrementCount(key, counterType.getType(), num);
    }

    @Override
    public void incrementCount(String key, String field, Integer num) throws CounterException {
        try {
            redisService.hincreby(key, field, num);
        }catch (Exception e){
            e.printStackTrace();
            throw new CounterException("change count has occured an exception，"+e.getMessage());
        }
    }
}
