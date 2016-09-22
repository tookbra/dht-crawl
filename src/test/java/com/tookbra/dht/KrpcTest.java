package com.tookbra.dht;

import com.tookbra.dht.common.KrpcUtil;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by tookbra on 2016/8/8.
 */
public class KrpcTest {
    @Test
    public void findNode() throws IOException, NoSuchAlgorithmException {
        System.out.println(new String(KrpcUtil.findNode(null),"UTF-8"));
    }
}
