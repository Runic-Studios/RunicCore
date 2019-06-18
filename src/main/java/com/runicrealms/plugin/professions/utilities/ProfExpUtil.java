package com.runicrealms.plugin.professions.utilities;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.NumRounder;

/**
 * Utility to grant player profession experience and keep track of it.
 * Since switching between the class lv / proff lv on the actual exp bar
 * proved to be too vulnerable to bugs, I included some math to mimic the
 * vanilla (class) leveling experience for professions.
 * Values taken from: https://minecraft.gamepedia.com/Experience
 * @author Skyfallin_
 */
public class ProfExpUtil {

    private static final int maxLevel = 50;

    public static void giveExperience(Player pl, int expGained) {

        String profName = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.prof.name");
        int currentLv = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");
        int currentExp = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.exp");

        if (currentLv >= maxLevel) return;

        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.prof.exp", currentExp + expGained);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
        int newTotalExp = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.exp");

        if (calculateExpectedLv(newTotalExp) != currentLv) {

            RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.prof.level", calculateExpectedLv(newTotalExp));
            RunicCore.getInstance().saveConfig();
            RunicCore.getInstance().reloadConfig();
            currentLv = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");

            RunicCore.getScoreboardHandler().updateSideInfo(pl);

            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

            // title message
            if (currentLv == maxLevel) {
                pl.sendTitle(
                        ChatColor.GOLD + "Max Level!",
                        ChatColor.GOLD + profName + " Level " + ChatColor.WHITE + currentLv, 10, 40, 10);
            } else {
                pl.sendTitle(
                        ChatColor.GREEN + "Level Up!",
                        ChatColor.GREEN + profName + " Level " + ChatColor.WHITE + currentLv, 10, 40, 10);
            }
        }

        // calculate the player's progress towards the next level
        currentExp = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.exp");
        int totalExpAtLevel = calculateTotalExperience(currentLv);
        int totalExpToLevel = calculateTotalExperience(currentLv+1);

        double progress = (double) (currentExp-totalExpAtLevel) / (totalExpToLevel-totalExpAtLevel);
        int progressRounded = (int) NumRounder.round(progress * 100);

        pl.sendMessage(ChatColor.GREEN + "Progress towards next lv: " + progressRounded + "% "
                + "(" + (currentExp-totalExpAtLevel) + "/" + (totalExpToLevel-totalExpAtLevel) + ")");
    }

    private static int calculateExpectedLv(int experience) {

        int expectedLv = 0;

        if (experience <= 352) { // lv 0-16
            for (int x = 0; x < 17 ; x++) {
                if (((x*x)+6*x) <= experience) {
                    expectedLv = x;
                }
            }
        } else if (experience <= 1507) { // lv 17-31
            for (int x = 17; x < 32; x++) {
                if (((2.5*x*x)-40.5*x)+360 <= experience) {
                    expectedLv = x;
                }
            }
        } else {
            for (int x = 32; x <= maxLevel; x++) { // lv 32+
                if (((4.5*x*x)-162.5*x)+2220 <= experience) {
                    expectedLv = x;
                }
            }
        }
        return expectedLv;
    }

    private static int calculateTotalExperience(int currentLv) {
        int totalExp;
        if (currentLv < 17) {
            totalExp = (currentLv*currentLv)+6*currentLv;
        } else if (currentLv < 32) {
            totalExp = (int) ((2.5*(currentLv*currentLv))-(40.5*currentLv)+360);
        } else {
            totalExp = (int) ((4.5*(currentLv*currentLv))-(162.5*currentLv)+2220);
        }
        return totalExp;
    }
}
