package com.runicrealms.plugin.player.cache;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Big boi class. Caches and stores all info about a player which must be written to config.
 */
// todo: add hunter info
public class PlayerCache {

    private UUID playerID;
    private String guild;
    private String className;
    private String profName;

    private int classLevel;
    private int classExp;
    private int profLevel;
    private int profExp;

    private int currentHealth;
    private int maxMana;

    private boolean isOutlaw;
    private int rating;

    private ItemStack[] inventoryContents;
    private Location location;

    /**
     * Created when a player selects a class for the first time
     */
    public PlayerCache(UUID playerID, String className) {
        this.playerID = playerID;
        this.guild = "None";
        this.className = className;
        this.profName = "None";
        this.classLevel = 0;
        this.classExp = 0;
        this.profLevel = 0;
        this.profExp = 0;
        this.currentHealth = HealthUtils.getBaseHealth();
        this.maxMana = RunicCore.getManaManager().getBaseMana();
        this.isOutlaw = false;
        this.rating = RunicCore.getOutlawManager().getBaseRating();
        this.inventoryContents = new ItemStack[]{}; // empty inventory
        this.location = new Location(Bukkit.getWorld("Alterra"), -2317.5, 38.5, 1719.5); // tutorial
    }

    /**
     * For players who already have a profile.
     * @param playerID uuid of player
     * @param guild uuid of player's guild
     * @param className name of player's class
     * @param profName name of player's profession
     * @param classLevel player's class level
     * @param classExp player's total class exp
     * @param profLevel player's profession level
     * @param profExp player's total prof exp
     * @param currentHealth player's health at time of object call
     * @param maxMana player's max mana
     * @param isOutlaw player's outlaw status
     * @param rating player's outlaw rating
     */
    public PlayerCache(UUID playerID, String guild, String className, String profName,
                       int classLevel, int classExp, int profLevel, int profExp,
                       int currentHealth, int maxMana,
                       boolean isOutlaw, int rating,
                       ItemStack[] inventoryContents, Location location) {
        this.playerID = playerID;
        this.guild = guild;
        this.className = className;
        this.profName = profName;
        this.classLevel = classLevel;
        this.classExp = classExp;
        this.profLevel = profLevel;
        this.profExp = profExp;
        this.currentHealth = currentHealth;
        this.maxMana = maxMana;
        this.isOutlaw = isOutlaw;
        this.rating = rating;
        this.inventoryContents = inventoryContents;
        this.location = location;
    }

    public UUID getPlayerID() {
        return playerID;
    }

    public void setPlayerID(UUID playerID) {
        this.playerID = playerID;
    }

    public String getGuild() {
        return guild;
    }

    public void setGuild(String guild) {
        this.guild = guild;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getProfName() {
        return profName;
    }

    public void setProfName(String profName) {
        this.profName = profName;
    }

    public int getClassLevel() {
        return classLevel;
    }

    public void setClassLevel(int classLevel) {
        this.classLevel = classLevel;
    }

    public int getClassExp() {
        return classExp;
    }

    public void setClassExp(int classExp) {
        this.classExp = classExp;
    }

    public int getProfLevel() {
        return profLevel;
    }

    public void setProfLevel(int profLevel) {
        this.profLevel = profLevel;
    }

    public int getProfExp() {
        return profExp;
    }

    public void setProfExp(int profExp) {
        this.profExp = profExp;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
    }

    public boolean getIsOutlaw() {
        return isOutlaw;
    }

    public void setOutlaw(boolean outlaw) {
        isOutlaw = outlaw;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public ItemStack[] getInventoryContents() {
        return inventoryContents;
    }

    public void setInventoryContents(ItemStack[] inventoryContents) {
        this.inventoryContents = inventoryContents;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
