package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import redis.clients.jedis.Jedis;

import java.util.UUID;

/**
 * Intermediary object used to read data from mongo or redis and then store data back in redis.
 * Destroyed after use
 *
 * @author Skyfallin
 */
public class CharacterData {

    private final BaseCharacterData baseCharacterData;
    private final ClassData classData;
    private final OutlawData outlawData;

    /**
     * Build the player's character data from mongo and add it to redis
     *
     * @param uuid            of the player that selected the character profile
     * @param slot            the chosen character slot from the select screen
     * @param playerMongoData associated with the player's unique id
     * @param jedis           the jedis resource
     */
    public CharacterData(UUID uuid, int slot, PlayerMongoData playerMongoData, Jedis jedis) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        this.baseCharacterData = new BaseCharacterData(uuid, slot, character);
        this.classData = new ClassData(uuid, character);
        this.outlawData = new OutlawData(uuid, character);
        writeCharacterDataToJedis(jedis);
    }

    /**
     * Build the player's character data from redis
     *
     * @param uuid  of the player that selected the character profile
     * @param slot  the chosen character slot from the select screen
     * @param jedis the jedis resource
     */
    public CharacterData(UUID uuid, int slot, Jedis jedis) {
        this.baseCharacterData = new BaseCharacterData(uuid, slot, jedis);
        this.classData = new ClassData(uuid, slot, jedis);
        this.outlawData = new OutlawData(uuid, slot, jedis);
    }

    public BaseCharacterData getBaseCharacterInfo() {
        return baseCharacterData;
    }

    public ClassData getClassInfo() {
        return classData;
    }

    public OutlawData getOutlawInfo() {
        return outlawData;
    }

    /**
     * Stores data in jedis/redis for caching session data
     *
     * @param jedis the jedis resource
     */
    public void writeCharacterDataToJedis(Jedis jedis) {
        this.baseCharacterData.writeToJedis(jedis, this.baseCharacterData.getSlot());
        this.classData.writeToJedis(jedis, this.baseCharacterData.getSlot());
        this.outlawData.writeToJedis(jedis, this.baseCharacterData.getSlot());
        String uuid = String.valueOf(baseCharacterData.getUuid());
        String key = uuid + ":character:" + baseCharacterData.getSlot();
        jedis.expire(key, RunicCore.getRedisAPI().getExpireTime());
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
            this.outlawData.writeToMongo(playerMongoData, slot);
        } catch (Exception e) {
            RunicCore.getInstance().getLogger().info("[ERROR]: There was a problem writing character data to mongo!");
            e.printStackTrace();
        }
    }
}
