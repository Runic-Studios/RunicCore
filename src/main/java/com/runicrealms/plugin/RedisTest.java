package com.runicrealms.plugin;

import org.bukkit.Bukkit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisTest {

    public static JedisPool JEDIS_POOL;

    public RedisTest() {

        Jedis jedis = new Jedis("redis-17083.c15.us-east-1-2.ec2.cloud.redislabs.com", 17083);
        jedis.auth("i3yIgvdVw13MbFO2RJF382dX8kvZzUaD");

        // Pipeline (like threading)
//        Pipeline pipeline = jedis.pipelined();
//        pipeline.set("Potato", "My Amazing Potato Object");
//        pipeline.expire("Potato", 86400); // 24h
//        pipeline.persist("Potato");
//        pipeline.sync();
//        pipeline.close();

        JEDIS_POOL = new JedisPool("redis-17083.c15.us-east-1-2.ec2.cloud.redislabs.com", 17083);

        try (Jedis j = JEDIS_POOL.getResource()) { // try-with-resources to close the connection for us
            // If you want to use a password, use
            j.auth("i3yIgvdVw13MbFO2RJF382dX8kvZzUaD");
            j.set("key", "value");
            // getLogger.info(j.get("key"));
        } finally {
            Bukkit.getLogger().info("Keyword: " + jedis.get("Potato"));
        }
        // Be sure to close it! It can and will cause memory leaks.
        jedis.close();
    }
}
