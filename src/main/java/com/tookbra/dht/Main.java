package com.tookbra.dht;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.concurrent.locks.LockSupport;

/**
 * Created by tookbra on 2016/7/28.
 */
public class Main {
    public static ApplicationContext applicationContext;

    public static void main(String [] args) {
        applicationContext = new ClassPathXmlApplicationContext("spring/*.xml");
//        LockSupport.park();
    }
}
