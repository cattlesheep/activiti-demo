package com.law.activitidemo.hash;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author Law
 * @version 1.0
 * @description 一致性hash算法
 * @date 2019-02-26 15:23
 */
public class ConsistentHash<T> {
    //hash函数
    private final HashFunction hashFunction;
    //虚拟节点数()
    private final int virtualNodeNum;
    //hash环
    private final SortedMap<Integer,T> cricle = new TreeMap<>();

    public ConsistentHash(HashFunction hashFunction, int virtualNodeNum, Collection<T> nodes){
        this.hashFunction = hashFunction;
        this.virtualNodeNum = virtualNodeNum;
        this.add(nodes);
    }

    /**
     * 添加节点集到环上
     * @param nodes
     */
    public void add(Collection<T> nodes) {
        for(T node:nodes){
            this.add(node);
        }
    }

    /**
     * 将节点加到环上
     * @param node
     */
    private void add(T node) {
        for(int i=0;i<virtualNodeNum;i++){
            String key = node.toString() + "_VN"+i;
            int hash = this.hashFunction.hashString(key.toString(), Charsets.UTF_8).asInt();
            cricle.put(hash,node);
        }

    }

    /**
     * 移除节点
     * @param nodes
     */
    public void remove(Collection<T> nodes){
        for(T node:nodes){
            this.remove(node);
        }
    }

    /**
     * 移除节点
     * @param node
     */
    private void remove(T node) {
        for(int i=0;i<virtualNodeNum;i++){
            String key = node.toString() + "_VN"+i;
            int hash = this.hashFunction.hashString(key.toString(), Charsets.UTF_8).asInt();
            cricle.remove(hash);
        }
    }

    /**
     * 获取服务器
     * @param key
     * @return
     */
    public T getNode(Object key){
        if(key==null){
            return null;
        }
        //获取key的hash值
        int hash = this.hashFunction.hashString(key.toString(), Charsets.UTF_8).asInt();
        if(!cricle.containsKey(hash)){
            //如果未命中，则顺时针向前推进，找到第一个节点，最后一个节点之后，则找到第一个节点
            SortedMap<Integer,T> tailMap = cricle.tailMap(hash);
            hash = tailMap.isEmpty()?cricle.firstKey():tailMap.firstKey();
        }
        return cricle.get(hash);
    }

    public static void main(String... agrs){
        Collection<String> services = new ArrayList<>();
        services.add("192.168.1.210:8080");
        services.add("192.168.1.211:8080");
        services.add("192.168.1.212:8080");
        //选取hash函数
        HashFunction hashFunction = Hashing.murmur3_128();
        ConsistentHash<String> consistentHash = new ConsistentHash<>(hashFunction,100,services);

        System.out.println(consistentHash.getNode("langpeng"));
        System.out.println(consistentHash.getNode("langpeng1"));
        System.out.println(consistentHash.getNode("langpeng2"));

    }

}
