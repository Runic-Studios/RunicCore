package us.fortherealm.plugin.professions.utilities;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.utilities.NumRounder;

public class ProfExpUtil {

    /**
     * Utility to grant player profession experience and keep track of it.
     * Since switching between the class lv / proff lv on the actual exp bar
     * proved to be too vulnerable to bugs, I included some math to mimic the
     * vanilla (class) leveling experience for professions.
     * Values taken from: https://minecraft.gamepedia.com/Experience
     * @author Skyfallin_
     */
    public static void giveExperience(Player pl, int expGained) {

        String profName = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.prof.name");
        int currentLv = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.level");
        int currentExp = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.exp");

        Main.getInstance().getConfig().set(pl.getUniqueId() + ".info.prof.exp", currentExp + expGained);
        Main.getInstance().saveConfig();
        Main.getInstance().reloadConfig();

        currentExp = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.prof.exp");

        // if the player breaches the total experience needed to level threshold,
        // level up!
        if (currentExp >= calculateTotalExperience(currentLv + 1)) {
            Main.getInstance().getConfig().set(pl.getUniqueId() + ".info.prof.level", currentLv + 1);
            Main.getInstance().saveConfig();
            Main.getInstance().reloadConfig();
            currentLv += 1;
            Main.getScoreboardHandler().updateSideInfo(pl);
            pl.playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            pl.sendTitle(
                    ChatColor.GREEN + "Level Up!",
                    ChatColor.GREEN + profName + " Level " + ChatColor.WHITE + currentLv, 10, 40, 10);
        }

        // calculate the player's progress towards the next level
        int expToNextLevel = calculateExpToLevel(currentLv);
        int totalExpAtLevel = calculateTotalExperience(currentLv);
        int totalExpToLevel = calculateTotalExperience(currentLv+1);

        double progress = (double) (currentExp - totalExpAtLevel) / expToNextLevel;
        int progressRounded = (int) NumRounder.round(progress * 100);

        pl.sendMessage(ChatColor.GREEN + "Progress towards next lv: " + progressRounded + "% "
                + "(" + currentExp + "/" + totalExpToLevel + ")");
    }

    private static int calculateExpToLevel(int currentLv) {
        int expToLevel;
        if (currentLv < 16) {
            expToLevel = (2*currentLv)+7;
        } else if (currentLv < 31) {
            expToLevel = (5*currentLv)-38;
        } else {
            expToLevel = (9*currentLv)-158;
        }
        return expToLevel;
    }

    private static int calculateTotalExperience(int currentLv) {
        int totalExp;
        if (currentLv < 16) {
            totalExp = (currentLv*currentLv)+6*currentLv;
        } else if (currentLv < 31) {
            totalExp = (int) ((2.5*(currentLv*currentLv))-(40.5*currentLv)+360);
        } else {
            totalExp = (int) ((4.5*(currentLv*currentLv))-(162.5*currentLv)+2220);
        }
        return totalExp;
    }
}
