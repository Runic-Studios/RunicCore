package com.runicrealms.plugin.player.combat;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.NametagUtil;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;

/**
 * Handles all the logic for when a player levels-up their primary class (archer, mage, etc.)
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {

        Player pl = e.getPlayer();
        if (pl.getLevel() > PlayerLevelUtil.getMaxLevel())
            return; // insurance
        PlayerCache playerCache = RunicCore.getCacheManager().getPlayerCaches().get(pl);

        // update player's level
        playerCache.setClassLevel(pl.getLevel());

        // grab the player's new info
        String className = playerCache.getClassName();
        if (className == null)
            return;
        int classLevel = playerCache.getClassLevel();

        HealthUtils.setPlayerMaxHealth(pl);
        HealthUtils.setHeartDisplay(pl);
        int playerHealth = (int) pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        pl.setHealth(playerHealth);
        pl.setFoodLevel(20);

        if (pl.getLevel() == 0)
            return;

        NametagUtil.updateNametag(pl);
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

        // title screen message
        if (pl.getLevel() >= PlayerLevelUtil.getMaxLevel()) {
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
        String className;
        try {
            className = RunicCore.getCacheManager().getPlayerCaches().get(pl).getClassName();
        } catch (Exception e) {
            return HealthUtils.getBaseHealth();
        }

        switch (className.toLowerCase()) {
            case "archer":
                return (PlayerLevelUtil.getArcherHpLv()*pl.getLevel()) + HealthUtils.getBaseHealth();
            case "cleric":
                return (PlayerLevelUtil.getClericHpLv()*pl.getLevel()) + HealthUtils.getBaseHealth();
            case "mage":
                return (PlayerLevelUtil.getMageHpLv()*pl.getLevel()) + HealthUtils.getBaseHealth();
            case "rogue":
                return (PlayerLevelUtil.getRogueHpLv()*pl.getLevel()) + HealthUtils.getBaseHealth();
            case "warrior":
                return (PlayerLevelUtil.getWarriorHpLv()*pl.getLevel()) + HealthUtils.getBaseHealth();
        }

        return HealthUtils.getBaseHealth();
    }
}
