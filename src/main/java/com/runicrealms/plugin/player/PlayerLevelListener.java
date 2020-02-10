package com.runicrealms.plugin.player;

import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import com.runicrealms.plugin.RunicCore;

/**
 * Handles all the logic for when a player levels-up their primary class (archer, mage, etc.)
 * "Scales" the player's artifact, unlocks spell slots, and more.
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {

        Player pl = e.getPlayer();

        if (pl.getLevel() > PlayerLevelUtil.getMaxLevel()) return;

        // update player's level
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.class.level", pl.getLevel());

        // grab the player's new info
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        if (className == null) return;
        int classLevel = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");

        HealthUtils.setPlayerMaxHealth(pl);
        HealthUtils.setHeartDisplay(pl);
        int playerHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        pl.setHealth(playerHealth);
        RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.currentHP", (int) pl.getHealth());
        pl.setFoodLevel(20);

        saveConfig(pl);

        if (pl.getLevel() == 0) return;

        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

        // title screen message
        if (pl.getLevel() == PlayerLevelUtil.getMaxLevel()) {
            pl.sendTitle(
                    ChatColor.GOLD + "Max Level!",
                    ChatColor.GOLD + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        } else {
            pl.sendTitle(
                    ChatColor.GREEN + "Level Up!",
                    ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        }
    }


    /**
     * This method is used to calculate how much HP the wearer has from items. So it subtracts the base hp of their
     * level. Everything uses GENERIC_MAX_HEALTH, so this is the simplest way I've done it for now.
     * @author Skyfallin
     */
    public static int getHpAtLevel(Player pl) {

        // grab the player's new info
        String className = RunicCore.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        if (className == null) return HealthUtils.getBaseHealth();

        switch (className.toLowerCase()) {
            case "archer":
                return (pl.getLevel()) + HealthUtils.getBaseHealth();
            case "cleric":
                return (2*pl.getLevel()) + HealthUtils.getBaseHealth();
            case "mage":
                return (pl.getLevel()) + HealthUtils.getBaseHealth();
            case "rogue":
                return (pl.getLevel()) + HealthUtils.getBaseHealth();
            case "warrior":
                return (2*pl.getLevel()) + HealthUtils.getBaseHealth();
        }

        return HealthUtils.getBaseHealth();
    }

    private void saveConfig(Player pl) {
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
        RunicCore.getScoreboardHandler().updateSideInfo(pl);
    }
}
