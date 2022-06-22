package com.runicrealms.plugin.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisManager {

    private static final int REDIS_PORT = 17083; // TODO: env var
    private static final String REDIS_CONNECTION_STRING = "redis-17083.c15.us-east-1-2.ec2.cloud.redislabs.com"; // TODO: should be environment var
    private static final String REDIS_PASSWORD = "i3yIgvdVw13MbFO2RJF382dX8kvZzUaD"; // TODO: env var
    private final JedisPool jedisPool;

    public RedisManager() {

        jedisPool = new JedisPool(REDIS_CONNECTION_STRING, REDIS_PORT);

        try (Jedis jedis = jedisPool.getResource()) { // try-with-resources to close the connection for us
            jedis.auth(REDIS_PASSWORD);
        }
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
