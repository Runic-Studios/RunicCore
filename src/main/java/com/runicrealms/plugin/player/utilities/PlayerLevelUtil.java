package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.utilities.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerLevelUtil {

    private static final int MAX_LEVEL = 60;

    private static final int ARCHER_HP_LV = 5;
    private static final int CLERIC_HP_LV = 7;
    private static final int MAGE_HP_LV = 4;
    private static final int ROGUE_HP_LV = 6;
    private static final int WARRIOR_HP_LV = 10;

    /*
    Here is our exp curve!
    At level 50, the player is ~ halfway to max, w/ 764,750
    At level 60, the player needs 1,262,700 total exp
     */
    public static int calculateTotalExp(int currentLv) {
        int cubed = (int) Math.pow((currentLv+5), 3);
        return ((23*cubed)/5)-575;
    }

    /*
    This method takes in an experience amount and returns the level which corresponds to that amount.
    i.e., passing 764,700 will return level 50.
     */
    public static int calculateExpectedLv(int experience) {
        return (int) Math.cbrt((((5 * experience)+2875) / 23)) - 5;
    }

    /**
     * Called when a player earns experience towards their combat class
     */
    public static void giveExperience(Player pl, int expGained) {

        UUID playerID = pl.getUniqueId();
        PlayerCache playerCache = RunicCore.getCacheManager().getPlayerCache(playerID);
        if (playerCache == null) return;

        String className = playerCache.getClassName();
        int currentLv = playerCache.getClassLevel();
        int currentExp = playerCache.getClassExp();

        if (currentLv >= MAX_LEVEL) return;

        currentExp = currentExp + expGained;
        playerCache.setClassExp(currentExp);

        int newTotalExp = playerCache.getClassExp();

        if (calculateExpectedLv(newTotalExp) != currentLv) {

            // apply milestones for 10, 20, 30, 40, 50, 60.
            boolean needsMilestone = applyMileStone(pl, currentLv, className, calculateExpectedLv(newTotalExp));

            // send a basic leveling message for all the levels that aren't milestones.
            // (10, 20, etc.)
            if (!needsMilestone) {
                sendLevelMessage(pl);
            }

            pl.setLevel(calculateExpectedLv(newTotalExp));
            RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).setClassLevel(calculateExpectedLv(newTotalExp));
            currentLv = RunicCore.getCacheManager().getPlayerCache(playerID).getClassLevel();
            RunicCore.getScoreboardHandler().updateSideInfo(pl);
        }

        int totalExpAtLevel = calculateTotalExp(currentLv);
        int totalExpToLevel = calculateTotalExp(currentLv+1);
        double proportion = (double) (currentExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (currentLv == MAX_LEVEL) {
            pl.setExp(0);
        }
        if (proportion < 0) {
            proportion = 0.0f;
        }
        pl.setExp((float) proportion);
    }

    // todo: add new level info
    private static boolean applyMileStone(Player pl, int oldLevel, String className, int classLevel) {
        if (classLevel >= MAX_LEVEL) {
            Bukkit.broadcastMessage(ChatColor.WHITE + "" + ChatColor.BOLD + pl.getName()
                    + ChatColor.GOLD + ChatColor.BOLD + " has reached level " + classLevel + " " + className + "!");
            pl.sendMessage("\n");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GOLD + "" + ChatColor.BOLD + "MAX LEVEL REACHED!");
            pl.sendMessage("\n");
            ClassUtil.launchFirework(pl, className);
        } else if (classLevel == 50) {
            pl.sendMessage("\n");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + " You've reached level " + classLevel + "!");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "     You can now access " + ChatColor.DARK_RED + "The Frozen Fortress!");
            pl.sendMessage("\n");
            return true;
        } else if (classLevel >= 10  && oldLevel < 10) {
            sendUnlockMessage(pl, 10, className, classLevel);
            return true;
        } else if (classLevel >= 20 && oldLevel < 20) {
            sendUnlockMessage(pl, 20, className, classLevel);
            return true;
        } else if (classLevel >= 30 && oldLevel < 30) {
            sendUnlockMessage(pl, 30, className, classLevel);
            return true;
        } else if (classLevel >= 40 && oldLevel < 40) {
            pl.sendMessage("\n");
            ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
            pl.sendMessage("\n");
            return true;
        }
        return false;
    }

    private static void sendLevelMessage(Player pl) {

        String className = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName();
        if (className == null) return;

        // save player hp, restore hp.food
        int hpPerLevel = 0;
        switch (className.toLowerCase()) {
            case "archer":
                hpPerLevel = ARCHER_HP_LV;
                break;
            case "cleric":
                hpPerLevel = CLERIC_HP_LV;
                break;
            case "mage":
                hpPerLevel = MAGE_HP_LV;
                break;
            case "rogue":
                hpPerLevel = ROGUE_HP_LV;
                break;
            case "warrior":
                hpPerLevel = WARRIOR_HP_LV;
                break;
        }

        pl.sendMessage("\n");
        ChatUtils.sendCenteredMessage(pl, ChatColor.GREEN + "" + ChatColor.BOLD + "LEVEL UP!");
        ChatUtils.sendCenteredMessage(pl,
                ChatColor.RED + "" + ChatColor.BOLD + "+" + hpPerLevel + "❤ "
                        + ChatColor.DARK_AQUA + "+" + RunicCore.getManaManager().getManaPerLv(pl) + "✸");
        pl.sendMessage("\n");
    }

    private static void sendUnlockMessage(Player pl, int lvl, String className, int classLevel) {
        pl.sendTitle(
                ChatColor.GREEN + "Level Up!",
                ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        pl.sendMessage("\n");
//        ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "" + ChatColor.BOLD + "+1 Spell Point");
//        ChatUtils.sendCenteredMessage(pl, ChatColor.GRAY + "        You've unlocked a new artifact skin!");
//        ChatUtils.sendCenteredMessage(pl, ChatColor.WHITE + "      Click " + ChatColor.GREEN + "your Artifact or Rune to add a spell!");
        pl.sendMessage("\n");
    }

    public static int getMaxLevel() {
        return MAX_LEVEL;
    }

    public static int getArcherHpLv() {
        return ARCHER_HP_LV;
    }

    public static int getClericHpLv() {
        return CLERIC_HP_LV;
    }

    public static int getMageHpLv() {
        return MAGE_HP_LV;
    }

    public static int getRogueHpLv() {
        return ROGUE_HP_LV;
    }

    public static int getWarriorHpLv() {
        return WARRIOR_HP_LV;
    }
}
