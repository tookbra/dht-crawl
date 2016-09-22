package com.tookbra.dht.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tookbra on 2016/8/4.
 */
public class PingTask {

    private static final ExecutorService pingThreadPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("biz-%s").build());
}
