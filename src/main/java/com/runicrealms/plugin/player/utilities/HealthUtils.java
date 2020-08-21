package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GearScanner;
import de.tr7zw.nbtapi.NBTCompoundList;
import de.tr7zw.nbtapi.NBTEntity;
import de.tr7zw.nbtapi.NBTListCompound;
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
        String className = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName();

        // for new players
        if (className == null) {
            setBaseHealth(pl);
            return;
        }

        // grab player's level
        int classLevel = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassLevel();

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

        int total = BASE_HEALTH + (hpPerLevel * classLevel) + GearScanner.getHealthBoost(pl);

        HealthUtils.setHealthAttribute(pl, total);
        HealthUtils.setHeartDisplay(pl);
    }

    private static void setHealthAttribute(Player pl, double amt) {
        NBTEntity nbtPlayer = new NBTEntity(pl);
        NBTCompoundList list = nbtPlayer.getCompoundList("Attributes");
        for (int i = 0; i < list.size(); i++) {
            NBTListCompound lc = list.get(i);
            if (lc.getString("Name").equals("generic.maxHealth")) {
                lc.setDouble("Base", amt);
            }
        }
    }

    public static void setHeartDisplay(Player pl) {

        // retrieve player health
        int playerHealth = (int) Objects.requireNonNull(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();

        // todo: come up w/ a better system for this
//        int valuePerHeart;
//        switch (RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName()) {
//            case "Archer":
//                valuePerHeart = 3;
//                break;
//            case "Cleric":
//                valuePerHeart = 45;
//                break;
//            case "Mage":
//                valuePerHeart = 40;
//                break;
//            case "Rogue":
//                valuePerHeart = 40;
//                break;
//            case "Warrior":
//                valuePerHeart = 50;
//                break;
//            default:
//                valuePerHeart = 1;
//                break;
//        }
//
//        // a half-heart per 12.5 health
//        int numOfHalfHearts = (playerHealth / valuePerHeart) * 2;
//
//        // to prevent awkward half-heart displays, it rounds down to the nearest full heart.
//        if (numOfHalfHearts % 2 != 0) numOfHalfHearts = numOfHalfHearts - 1;
//
//        // insurance to prevent "greater than 0" errors on first join
//        if (numOfHalfHearts <= 0) numOfHalfHearts = 4;
//
//        pl.setHealthScale(numOfHalfHearts);
        pl.setHealthScale(20);
    }

    public static int getBaseHealth() {
        return BASE_HEALTH;
    }
}
