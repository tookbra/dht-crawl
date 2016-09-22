package com.tookbra.dht.redis.queue;

import java.io.Serializable;

/**
 * @author tookbra
 * @date 2016/9/9
 */
public interface Queue <T extends Serializable> {

    public void consume(T message);

    public void produce(T message);
}
