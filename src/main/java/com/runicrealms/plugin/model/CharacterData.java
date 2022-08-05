package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.redis.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * Intermediary object used to read data from mongo or redis and then store data back in redis.
 * Destroyed after use
 *
 * @author Skyfallin
 */
public class CharacterData {

    private final BaseCharacterData baseCharacterData;
    private final ClassData classData;
    private final ProfessionData professionData;
    private final OutlawData outlawData;

    /**
     * Build the player's character data from mongo and add it to redis
     *
     * @param uuid            of the player that selected the character profile
     * @param slot            the chosen character slot from the select screen
     * @param playerMongoData associated with the player's unique id
     */
    public CharacterData(UUID uuid, int slot, PlayerMongoData playerMongoData) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        this.baseCharacterData = new BaseCharacterData(uuid, slot, character);
        this.classData = new ClassData(uuid, character);
        this.professionData = new ProfessionData(uuid, character);
        this.outlawData = new OutlawData(uuid, character);
        writeCharacterDataToJedis(RunicCore.getRedisManager().getJedisPool());
    }

    /**
     * Build the player's character data from redis
     *
     * @param uuid  of the player that selected the character profile
     * @param slot  the chosen character slot from the select screen
     * @param jedis the jedis resource
     */
    public CharacterData(UUID uuid, int slot, Jedis jedis) {
        List<String> fields = new ArrayList<>();
        Map<String, String> fieldsMap = new HashMap<>();
        fields.addAll(BaseCharacterData.getFields());
        fields.addAll(ClassData.getFields());
        fields.addAll(ProfessionData.getFields());
        fields.addAll(OutlawData.getFields());
        String[] fieldsToArray = fields.toArray(new String[0]);
        List<String> values = jedis.hmget(uuid + ":character:" + slot, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(fieldsToArray[i], values.get(i));
        }
        this.baseCharacterData = new BaseCharacterData(uuid, fieldsMap);
        this.classData = new ClassData(uuid, fieldsMap);
        this.professionData = new ProfessionData(uuid, fieldsMap);
        this.outlawData = new OutlawData(uuid, fieldsMap);
    }

    /**
     * Used for the round trip to MongoDB. Saves all the data in the object to the database as nested fields
     *
     * @param playerMongoData of the player
     * @param slot            of the character
     */
    public void writeCharacterDataToMongo(PlayerMongoData playerMongoData, int slot) {
        try {
            this.baseCharacterData.writeToMongo(playerMongoData, slot);
            this.classData.writeToMongo(playerMongoData, slot);
            this.professionData.writeToMongo(playerMongoData, slot);
            this.outlawData.writeToMongo(playerMongoData, slot);
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: There was a problem writing character data to mongo!");
            e.printStackTrace();
        }
    }

    /**
     * Stores data in jedis/redis for caching session data
     *
     * @param jedisPool the JedisPool from the RedisManager
     */
    public void writeCharacterDataToJedis(JedisPool jedisPool) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.auth(RedisManager.REDIS_PASSWORD);
            String uuid = String.valueOf(baseCharacterData.getUuid());
            String key = uuid + ":character:" + baseCharacterData.getSlot();
            jedis.hmset(key, baseCharacterData.toMap());
            jedis.hmset(key, classData.toMap());
            jedis.hmset(key, professionData.toMap());
            jedis.hmset(key, outlawData.toMap());
            jedis.expire(key, RedisUtil.EXPIRE_TIME);
        }
    }

    public BaseCharacterData getBaseCharacterInfo() {
        return baseCharacterData;
    }

    public ClassData getClassInfo() {
        return classData;
    }

    public ProfessionData getProfessionInfo() {
        return professionData;
    }

    public OutlawData getOutlawInfo() {
        return outlawData;
    }
}
