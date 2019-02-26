package com.law.activitidemo;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Law
 * @version 1.0
 * @description TODO
 * @date 2019-02-26 9:45
 */
@SpringBootTest
public class ConsistentHashingTest {
    @Test
    public void springHashTest(){
        System.out.println("192.168.0.0:111&&VN1的哈希值：" + getHash("192.168.0.0:111&&VN1"));
        System.out.println("192.168.0.0:111&&VN2的哈希值：" + getHash("192.168.0.0:111&&VN2"));
        System.out.println("192.168.0.0:111&&VN3的哈希值：" + getHash("192.168.0.0:111&&VN3"));
        System.out.println("192.168.0.0:111&&VN4的哈希值：" + getHash("192.168.0.0:111&&VN4"));
        System.out.println("192.168.0.0:111&&VN5的哈希值：" + getHash("192.168.0.0:111&&VN5"));


        System.out.println("192.168.0.1:111的哈希值：" + "192.168.0.1:1111".hashCode());
        System.out.println("192.168.0.2:111的哈希值：" + "192.168.0.2:1111".hashCode());
        System.out.println("192.168.0.3:111的哈希值：" + "192.168.0.3:1111".hashCode());
        System.out.println("192.168.0.4:111的哈希值：" + "192.168.0.4:1111".hashCode());
    }

    public static int getHash(String str) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < str.length(); i++)
            hash = (hash ^ str.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;

        // 如果算出来的值为负数则取其绝对值
        if (hash < 0)
            hash = Math.abs(hash);
        return hash;
    }
    @Test
    public void testIntMax(){
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Math.pow(2,32)-1);
    }
}
