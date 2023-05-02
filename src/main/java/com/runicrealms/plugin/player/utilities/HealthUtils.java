package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

/**
 * This class controls the changing of the player's health,
 * as well as the way their hearts are displayed.
 *
 * @author Skyfallin_
 */
public class HealthUtils {

    private static final int BASE_HEALTH = 200;
    private static final int HEART_AMOUNT = 20;

    public static void setPlayerMaxHealth(Player player) {

        // Grab the player's new info
        String className = RunicCore.getCharacterAPI().getPlayerClass(player); // todo: NULL

        // For new players
        if (className == null) {
            player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(BASE_HEALTH);
            return;
        }

        // grab player's level
        int classLevel = player.getLevel();

        // save player hp
        double hpPerLevel = PlayerLevelUtil.determineHealthLvByClass(className);
        double coefficient = PlayerLevelUtil.getHealthLevelCoefficient();

        int total = (int) (BASE_HEALTH + (coefficient * Math.pow(classLevel, 2)) + (hpPerLevel * classLevel) + RunicItemsAPI.getAddedPlayerStats(player.getUniqueId()).getAddedHealth());

        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(total);
        player.setHealthScale(HEART_AMOUNT);
    }

    public static int getBaseHealth() {
        return BASE_HEALTH;
    }

    public static int getHeartAmount() {
        return HEART_AMOUNT;
    }
}
