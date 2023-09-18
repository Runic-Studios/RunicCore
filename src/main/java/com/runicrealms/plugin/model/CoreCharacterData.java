package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.SafeZoneLocation;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import org.bukkit.Location;

public class CoreCharacterData {
    private CharacterClass classType;
    private int level;
    private int exp;
    private int currentHp;
    private Location location;

    @SuppressWarnings("unused")
    public CoreCharacterData() {
        // Default constructor for Spring
    }

    /**
     * A container of class info used to load a player's character profile
     *
     * @param classType the class of the character (e.g., Cleric)
     * @param level     the level of the character
     * @param exp       the exp of the character
     * @param currentHp the stored health of the character
     * @param location  the last location of the character
     */
    public CoreCharacterData(CharacterClass classType, int level, int exp, int currentHp, Location location) {
        this.classType = classType;
        this.level = level;
        this.exp = exp;
        this.currentHp = currentHp;
        this.location = location;
    }

    /**
     * Attempts to populate the document for a new character slot with default values
     *
     * @param corePlayerData of parent object of player data who created character
     * @param className      the name of the class
     * @param slot           the slot of the character
     */
    public static void createCoreCharacterData(CorePlayerData corePlayerData, String className, Integer slot, final WriteCallback callback) {

        Location location = SafeZoneLocation.TUTORIAL.getLocation();
        CoreCharacterData coreCharacterData = new CoreCharacterData
                (
                        CharacterClass.getFromName(className),
                        0,
                        0,
                        HealthUtils.getBaseHealth(),
                        location
                );
        corePlayerData.getCoreCharacterDataMap().put(slot, coreCharacterData);
        // Update CoreCharacterData in Mongo (Uses TaskChain)
        RunicCore.getCoreWriteOperation().updateCoreCharacterData
                (
                        corePlayerData.getUuid(),
                        slot,
                        coreCharacterData,
                        callback
                );
    }

    public CharacterClass getClassType() {
        return this.classType;
    }

    public void setClassType(CharacterClass classType) {
        this.classType = classType;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public int getExp() {
        return this.exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

}
