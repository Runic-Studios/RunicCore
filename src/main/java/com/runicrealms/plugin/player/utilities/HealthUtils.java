package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GearScanner;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

/**
 * This class controls the changing of the player's health,
 * as well as the way their hearts are displayed.
 * @author Skyfallin_
 */
public class HealthUtils {

    private static final int BASE_HEALTH = 200;
    private static final int HEART_AMOUNT = 20;

    public static void setPlayerMaxHealth(Player pl) {

        // grab the player's new info
        String className = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName();

        // for new players
        if (className == null) {
            pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(BASE_HEALTH);
            return;
        }

        // grab player's level
        int classLevel = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassLevel();

        // save player hp
        double hpPerLevel = PlayerLevelUtil.determineHealthLvByClass(className, false);
        double coefficient = PlayerLevelUtil.determineHealthLvByClass(className, true);

        int total = (int) (BASE_HEALTH + (coefficient * Math.pow(classLevel, 2)) + (hpPerLevel * classLevel) + GearScanner.getHealthBoost(pl));

        pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(total);
        pl.setHealthScale(HEART_AMOUNT);
    }

    public static int getBaseHealth() {
        return BASE_HEALTH;
    }

    public static int getHeartAmount() {
        return HEART_AMOUNT;
    }
}
