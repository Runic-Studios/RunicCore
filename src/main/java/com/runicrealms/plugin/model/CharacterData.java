package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.event.CacheSaveReason;
import com.runicrealms.plugin.database.event.MongoSaveEvent;
import com.runicrealms.plugin.redis.RedisField;
import com.runicrealms.plugin.redis.RedisManager;
import com.runicrealms.plugin.redis.RedisUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param player          to be loaded
     * @param slot            the chosen character slot from the select screen
     * @param playerMongoData associated with the player's unique id
     */
    public CharacterData(Player player, int slot, PlayerMongoData playerMongoData) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        this.baseCharacterData = new BaseCharacterData(player, slot, character);
        ClassData tempClassData;
        try { // the player data object already stores basic class info, so let's try to grab it from there first
            tempClassData = RunicCore.getDatabaseManager().getPlayerDataMap().get(player.getUniqueId()).getPlayerCharacters().get(slot);
        } catch (NullPointerException e) {
            tempClassData = new ClassData(character);
            e.printStackTrace();
        }
        this.classData = tempClassData;
        this.professionData = new ProfessionData(character);
        this.outlawData = new OutlawData(character);
        writeCharacterDataToJedis(RunicCore.getRedisManager().getJedisPool());
    }

    /**
     * Build the player's character data from redis
     *
     * @param player to be loaded
     * @param slot   the chosen character slot from the select screen
     * @param jedis  the jedis resource
     */
    public CharacterData(Player player, int slot, Jedis jedis) {
        List<RedisField> fields = new ArrayList<>();
        Map<RedisField, String> fieldsMap = new HashMap<>();
        fields.addAll(BaseCharacterData.getFields());
        fields.addAll(ClassData.getFields());
        fields.addAll(ProfessionData.getFields());
        fields.addAll(OutlawData.getFields());
        List<String> fieldsToString = RedisUtil.redisFieldsToStrings(fields);
        String[] fieldsToArray = fieldsToString.toArray(new String[0]);
        List<String> values = jedis.hmget(player.getUniqueId() + ":character:" + slot, fieldsToArray);
        for (int i = 0; i < fieldsToArray.length; i++) {
            fieldsMap.put(RedisField.getFromFieldString(fieldsToArray[i]), values.get(i));
        }
        this.baseCharacterData = new BaseCharacterData(fieldsMap);
        this.classData = new ClassData(fieldsMap);
        this.professionData = new ProfessionData(fieldsMap);
        this.outlawData = new OutlawData(fieldsMap);
    }

    /**
     * Used for the round trip to MongoDB. Saves all the data in the object to the database as nested fields
     *
     * @param player          the player to save
     * @param cacheSaveReason save reason (shutdown, logout, etc.)
     */
    public void writeCharacterDataToMongo(Player player, CacheSaveReason cacheSaveReason) {
        try {
            int slot = RunicCore.getDatabaseManager().getLoadedCharactersMap().get(player.getUniqueId());
            PlayerMongoData mongoData = new PlayerMongoData(player.getUniqueId().toString());
            mongoData.set("last_login", LocalDate.now());
            PlayerMongoDataSection character = mongoData.getCharacter(slot);
            MongoSaveEvent e = new MongoSaveEvent(player, mongoData, character, cacheSaveReason);
            Bukkit.getPluginManager().callEvent(e);
            if (e.isCancelled()) return;
            this.baseCharacterData.writeToMongo(character);
            this.classData.writeToMongo(character);
            this.professionData.writeToMongo(character);
            this.outlawData.writeToMongo(character);
            // save data (includes nested fields)
            mongoData.save();
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
            String uuid = String.valueOf(baseCharacterData.getPlayerUuid());
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
