package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.PlayerMongoData;
import com.runicrealms.plugin.database.PlayerMongoDataSection;
import com.runicrealms.plugin.database.util.DatabaseUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

/**
 * Intermediary object used to read data from mongo or redis and then store data back in redis.
 * Destroyed after use
 *
 * @author Skyfallin
 */
public class CharacterData {

    private static final long EXPIRE_TIME = 86400; // seconds (24 hours)
    private final BaseCharacterInfo baseCharacterInfo;
    private final ClassInfo classInfo;
    private final ProfessionInfo professionInfo;
    private final OutlawInfo outlawInfo;

    /**
     * Build basic info about the player for the character select screen from mongo
     *
     * @param player          to be loaded
     * @param slot            the chosen character slot from the select screen
     * @param playerMongoData associated with the player's unique id
     */
    public CharacterData(Player player, int slot, PlayerMongoData playerMongoData) {
        PlayerMongoDataSection character = playerMongoData.getCharacter(slot);
        int currentHealth = character.get("currentHP", Integer.class);
        int maxMana = character.get("maxMana", Integer.class);
        int storedHunger = character.get("storedHunger", Integer.class) != null ? character.get("storedHunger", Integer.class) : 20;
        String profName = character.get("prof.name", String.class);
        int profLevel = character.get("prof.level", Integer.class);
        int profExp = character.get("prof.exp", Integer.class);
        boolean isOutlaw = character.get("outlaw.enabled", Boolean.class);
        int rating = character.get("outlaw.rating", Integer.class);
        Location location = DatabaseUtil.loadLocation(player, character);
        this.baseCharacterInfo = new BaseCharacterInfo(slot, currentHealth, maxMana, storedHunger, player.getUniqueId(), location);
        ClassInfo tempClassInfo;
        try { // the player data object already stores basic class info, so let's try to grab it from there first
            tempClassInfo = RunicCore.getCacheManager().getPlayerDataMap().get(player.getUniqueId()).getPlayerCharacters().get(slot);
        } catch (NullPointerException e) {
            tempClassInfo = new ClassInfo
                    (
                            ClassEnum.getFromName(character.get("class.name", String.class)),
                            character.get("class.level", Integer.class),
                            character.get("class.exp", Integer.class)
                    );
            e.printStackTrace();
        }
        this.classInfo = tempClassInfo;
        this.professionInfo = new ProfessionInfo(profName, profExp, profLevel);
        this.outlawInfo = new OutlawInfo(isOutlaw, rating);
        writeCharacterDataToJedis(RunicCore.getRedisManager().getJedisPool().getResource());
    }

    // todo: build from redis
//    public CharacterData(Player player, Jedis jedis) {
//        this.baseCharacterInfo = new BaseCharacterInfo();
//        this.classInfo = new ClassInfo();
//        this.professionInfo = new ProfessionInfo();
//        this.outlawInfo = new OutlawInfo();
//    }

    // todo: write object to mongo

    // todo: write object to redis

    /**
     * Stores data in jedis/redis for caching
     *
     * @param jedis the JedisPool resource from the RedisManager
     */
    public void writeCharacterDataToJedis(Jedis jedis) {
        String uuid = String.valueOf(baseCharacterInfo.getPlayerUuid());
        String key = uuid + ":character:" + baseCharacterInfo.getSlot();
        jedis.hmset(key, baseCharacterInfo.toMap());
        jedis.hmset(key, classInfo.toMap());
        jedis.hmset(key, professionInfo.toMap());
        jedis.hmset(key, outlawInfo.toMap());
        jedis.expire(key, EXPIRE_TIME);
    }

    public BaseCharacterInfo getBaseCharacterInfo() {
        return baseCharacterInfo;
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public ProfessionInfo getProfessionInfo() {
        return professionInfo;
    }

    public OutlawInfo getOutlawInfo() {
        return outlawInfo;
    }
}
