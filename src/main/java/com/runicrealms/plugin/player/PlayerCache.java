package com.runicrealms.plugin.player;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.player.utilities.HealthUtils;

import java.util.UUID;

/**
 * Big boi class. Caches and stores all info about a player which must be written to config.
 */
// todo: add hunter info
public class PlayerCache {

    private String className;
    private String profName;
    private UUID guildID;

    private int classLevel;
    private int classExp;
    private int profLevel;
    private int profExp;

    private int currentHealth;
    private int maxMana;

    private boolean isOutlaw;
    private int rating;

    /**
     * Created when a player selects a class for the first time
     */
    public PlayerCache(String className) {
        this.className = className;
        this.profName = "None";
        this.guildID = null;
        this.classLevel = 0;
        this.classExp = 0;
        this.profLevel = 0;
        this.profExp = 0;
        this.currentHealth = HealthUtils.getBaseHealth();
        this.maxMana = RunicCore.getManaManager().getBaseMana();
        this.isOutlaw = false;
        this.rating = OutlawManager.getBaseRating();
    }

    /**
     * For players who already have a profile.
     * @param className name of player's class
     * @param profName name of player's profession
     * @param guildID uuid of player's guild
     * @param classLevel player's class level
     * @param classExp player's total class exp
     * @param profLevel player's profession level
     * @param profExp player's total prof exp
     * @param currentHealth player's health at time of object call
     * @param maxMana player's max mana
     * @param isOutlaw player's outlaw status
     * @param rating player's outlaw rating
     */
    public PlayerCache(String className, String profName, UUID guildID,
                       int classLevel, int classExp, int profLevel, int profExp,
                       int currentHealth, int maxMana,
                       boolean isOutlaw, int rating) {
        this.className = className;
        this.profName = profName;
        this.guildID = guildID;
        this.classLevel = classLevel;
        this.classExp = classExp;
        this.profLevel = profLevel;
        this.profExp = profExp;
        this.currentHealth = currentHealth;
        this.maxMana = maxMana;
        this.isOutlaw = isOutlaw;
        this.rating = rating;
    }
}
