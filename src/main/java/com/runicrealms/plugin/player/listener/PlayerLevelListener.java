package com.runicrealms.plugin.player.listener;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.redis.RedisField;
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
 *
 * @author Skyfallin_
 */
public class PlayerLevelListener implements Listener {

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {

        Player player = e.getPlayer();
        if (player.getLevel() > PlayerLevelUtil.getMaxLevel()) return; // insurance

        // update player's level in redis
        RunicCoreAPI.setRedisValue(player, RedisField.CLASS_LEVEL, String.valueOf(player.getLevel()));

        // grab the player's new info
        String className = RunicCoreAPI.getRedisValue(player, RedisField.CLASS_TYPE.getField());
        if (className.equals("")) return;
        int classLevel = player.getLevel();

        HealthUtils.setPlayerMaxHealth(player);
        player.setHealthScale(HealthUtils.getHeartAmount());
        int playerHealth = (int) player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        player.setHealth(playerHealth);
        player.setFoodLevel(20);

        if (player.getLevel() == 0) return;

        NametagUtil.updateNametag(player);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);

        // title screen message
        if (player.getLevel() >= PlayerLevelUtil.getMaxLevel()) {
            player.sendTitle(
                    ChatColor.GOLD + "Max Level!",
                    ChatColor.GOLD + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        } else {
            player.sendTitle(
                    ChatColor.GREEN + "Level Up!",
                    ChatColor.GREEN + className + " Level " + ChatColor.WHITE + classLevel, 10, 40, 10);
        }
    }

    /**
     * This method is used to calculate how much HP the wearer has from items. So it subtracts the base hp of their
     * level. Everything uses GENERIC_MAX_HEALTH, so this is the simplest way I've done it for now.
     *
     * @author Skyfallin
     */
    public static int getHpAtLevel(Player player) {

        // grab the player's new info
        String className;
        try {
            className = RunicCoreAPI.getRedisValue(player, RedisField.CLASS_TYPE.getField());
        } catch (Exception e) {
            return HealthUtils.getBaseHealth();
        }

        switch (className.toLowerCase()) {
            case "archer":
                return (PlayerLevelUtil.getArcherHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "cleric":
                return (PlayerLevelUtil.getClericHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "mage":
                return (PlayerLevelUtil.getMageHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "rogue":
                return (PlayerLevelUtil.getRogueHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
            case "warrior":
                return (PlayerLevelUtil.getWarriorHpLv() * player.getLevel()) + HealthUtils.getBaseHealth();
        }

        return HealthUtils.getBaseHealth();
    }
}
