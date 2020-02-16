package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import de.tr7zw.itemnbtapi.NBTEntity;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * This class controls the changing of the player's health,
 * as well as the way their hearts are displayed.
 * (ex: 50/12.5 = 4 hearts displayed)
 * @author Skyfallin_
 */
public class HealthUtils {

    private static final int BASE_HEALTH = 200;

    public static void setBaseHealth(Player pl) {
        setHealthAttribute(pl, BASE_HEALTH);
    }

    public static void setPlayerMaxHealth(Player pl) {

        // grab the player's new info
        String className = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName();

        // for new players
        if (className == null) {
            setBaseHealth(pl);
            return;
        }

        // grab player's level
        int classLevel = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel();

        // save player hp
        int hpPerLevel = 0;
        switch (className.toLowerCase()) {
            case "archer":
                hpPerLevel = PlayerLevelUtil.getArcherHpLv();
                break;
            case "cleric":
                hpPerLevel = PlayerLevelUtil.getClericHpLv();
                break;
            case "mage":
                hpPerLevel = PlayerLevelUtil.getMageHpLv();
                break;
            case "rogue":
                hpPerLevel = PlayerLevelUtil.getRogueHpLv();
                break;
            case "warrior":
                hpPerLevel = PlayerLevelUtil.getWarriorHpLv();
                break;
        }

        int total = BASE_HEALTH +(hpPerLevel*classLevel);

        HealthUtils.setHealthAttribute(pl, total);
        HealthUtils.setHeartDisplay(pl);
    }

    private static void setHealthAttribute(Player pl, double amt) {
        NBTEntity nbtPlayer = new NBTEntity(pl);
        NBTList list = nbtPlayer.getList("Attributes", NBTType.NBTTagCompound);
        for (int i = 0; i < list.size(); i++) {
            NBTListCompound lc = list.getCompound(i);
            if (lc.getString("Name").equals("generic.maxHealth")) {
                lc.setDouble("Base", amt);
            }
        }
    }

    public static void setHeartDisplay(Player pl) {

        // retrieve player health
        int playerHealth = (int) Objects.requireNonNull(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

        // a half-heart per 12.5 health
        int numOfHalfHearts = (playerHealth / 100) * 2;

        // to prevent awkward half-heart displays, it rounds down to the nearest full heart.
        if (numOfHalfHearts % 2 != 0) numOfHalfHearts = numOfHalfHearts - 1;

        // insurance to prevent "greater than 0" errors on first join
        if (numOfHalfHearts <= 0) numOfHalfHearts = 4;


        pl.setHealthScale(numOfHalfHearts);
    }

    public static int getBaseHealth() {
        return BASE_HEALTH;
    }
}
