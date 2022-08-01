package com.runicrealms.plugin.model;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.database.MongoData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This intermediary object acts as an easy way to pass data from mongo to memory to redis, or vice versa
 * It is used during login to both display data in the character select screen and load basic info for the player
 * on character select, then it is removed after.
 *
 * @author Skyfallin
 */
public class PlayerData {

    private static final String DATA_SECTION_KEY = "character";
    private final UUID playerUuid;
    private final String guild;
    private final Map<Integer, ClassData> playerCharacters;

    /**
     * Build basic info about the player for the character select screen from mongo
     *
     * @param player    to be loaded
     * @param mongoData associated with the player's unique id
     */
    public PlayerData(Player player, MongoData mongoData) {
        this.playerUuid = player.getUniqueId();
        this.guild = mongoData.get("guild", String.class);
        this.playerCharacters = new HashMap<>();
        try {
            if (mongoData.has(DATA_SECTION_KEY)) {
                Bukkit.broadcastMessage("characters found");
                for (String key : mongoData.getSection(DATA_SECTION_KEY).getKeys()) {
                    playerCharacters.put(Integer.parseInt(key), new ClassData(
                            ClassEnum.getFromName(mongoData.get(DATA_SECTION_KEY + "." + key + ".class.name", String.class)),
                            mongoData.get(DATA_SECTION_KEY + "." + key + ".class.level", Integer.class),
                            mongoData.get(DATA_SECTION_KEY + "." + key + ".class.exp", Integer.class)));

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public String getGuild() {
        return guild;
    }

    public Map<Integer, ClassData> getPlayerCharacters() {
        return playerCharacters;
    }
}
