package com.tookbra.dht;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tookbra on 2016/8/13.
 */
public class ScheduleExecTest {

    private static final ScheduledExecutorService findThreadPool = Executors.newScheduledThreadPool(1,new ThreadFactoryBuilder().setNameFormat("biz-%s").build());

    public static void main(String [] args) {
        findThreadPool.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println(System.currentTimeMillis());
            }
        },1,1,TimeUnit.SECONDS);
    }
}
