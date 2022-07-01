package com.runicrealms.plugin.model;

import com.runicrealms.plugin.database.util.DatabaseUtil;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BaseCharacterInfo implements JedisSerializable {
    private final int slot;
    private final int currentHp;
    private final int maxMana;
    private final int storedHunger;
    private final UUID playerUuid;
    private final Location location;

    /**
     * A container of basic info used to load a player character profile
     *
     * @param slot         the slot of the character (1 for first created profile)
     * @param currentHp    the stored health of the character
     * @param maxMana      the stored mana of the character
     * @param storedHunger the stored hunger of the character
     * @param playerUuid   the uuid of the player account
     * @param location     the last known location of the character
     */
    public BaseCharacterInfo(int slot, int currentHp, int maxMana, int storedHunger, UUID playerUuid, Location location) {
        this.slot = slot;
        this.currentHp = currentHp;
        this.maxMana = maxMana;
        this.storedHunger = storedHunger;
        this.playerUuid = playerUuid;
        this.location = location;
    }

    public int getSlot() {
        return slot;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getStoredHunger() {
        return storedHunger;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Location getLocation() {
        return location;
    }

    /**
     * Returns a map that can be used to set values in redis
     *
     * @return a map of string keys and character info values
     */
    @Override
    public Map<String, String> toMap() {
        return new HashMap<String, String>() {{
            put("slot", String.valueOf(slot));
            put("currentHp", String.valueOf(currentHp));
            put("maxMana", String.valueOf(maxMana));
            put("storedHunger", String.valueOf(storedHunger));
            put("playerUuid", String.valueOf(playerUuid));
            put("location", DatabaseUtil.serializeLocation(location));
        }};
    }

    @Override
    public <T> T fromMap(String key, Class<T> type) {
        return null;
    }
}
