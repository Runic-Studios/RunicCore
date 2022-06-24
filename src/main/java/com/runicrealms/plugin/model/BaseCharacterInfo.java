package com.runicrealms.plugin.model;

import org.bukkit.Location;

import java.util.UUID;

/**
 *
 */
public class BaseCharacterInfo {
    private final int slot;
    private final int currentHp;
    private final int maxMana;
    private final int storedHunger;
    private final UUID playerUuid;
    private final Location location;

    /**
     * @param slot
     * @param currentHp
     * @param maxMana
     * @param storedHunger
     * @param playerUuid
     * @param location
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
}
