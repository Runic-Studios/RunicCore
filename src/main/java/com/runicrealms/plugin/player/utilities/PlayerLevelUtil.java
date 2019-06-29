package com.runicrealms.plugin.player.utilities;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.entity.Player;

public class PlayerLevelUtil {

    private static final int maxLevel = 50;

    public static void giveExperience(Player pl, int expGained) {

        int currentLv = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");
        int currentExp = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.exp");

        if (currentLv >= maxLevel) return;

        currentExp = currentExp + expGained;
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.class.exp", currentExp);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
        int newTotalExp = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.exp");

        if (calculateExpectedLv(newTotalExp) != currentLv) {

            RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.class.level", calculateExpectedLv(newTotalExp));
            RunicCore.getInstance().saveConfig();
            RunicCore.getInstance().reloadConfig();
            currentLv = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");

            RunicCore.getScoreboardHandler().updateSideInfo(pl);

            pl.setLevel(currentLv);
        }

        int totalExpAtLevel = calculateTotalExp(currentLv);
        int totalExpToLevel = calculateTotalExp(currentLv+1);
        double proportion = (double) (currentExp - totalExpAtLevel) / (totalExpToLevel - totalExpAtLevel);
        if (currentLv == maxLevel) {
            pl.setExp(0);
        }
        if (proportion < 0) {
            proportion = 0.0f;
        }
        pl.setExp((float) proportion);
    }

    private static int calculateExpectedLv(int experience) {
        return (int) Math.cbrt((((5 * experience)+375) / 3)) - 5;
    }

    public static int calculateTotalExp(int currentLv) {
        int cubed = (int) Math.pow((currentLv+5), 3);
        return ((3*cubed)/5)-75;
    }
}
