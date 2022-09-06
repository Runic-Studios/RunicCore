package com.runicrealms.plugin.classes;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import com.runicrealms.plugin.model.ClassData;
import com.runicrealms.plugin.player.utilities.HealthUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import static org.bukkit.Color.*;

public class SelectClass {

    // sets the player artifact
    public static void setPlayerClass(Player player, String className, boolean isTutorial) {

        // build class-specific variables
        Color color = WHITE;
        switch (className.toLowerCase()) {
            case "archer":
                color = LIME;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Right-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Right-Click " + ChatColor.GRAY + "to cast barrage!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_GREEN + "You selected",
                            ChatColor.GREEN + "Archer!", 10, 40, 10);
                }
                break;
            case "cleric":
                color = AQUA;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast rejuvenate!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_AQUA + "You selected",
                            ChatColor.AQUA + "Cleric!", 10, 40, 10);
                }
                break;
            case "mage":
                color = FUCHSIA;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast blizzard!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_PURPLE + "You selected",
                            ChatColor.LIGHT_PURPLE + "Mage!", 10, 40, 10);
                }
                break;
            case "rogue":
                color = YELLOW;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast smoke bomb!");
                } else {
                    player.sendTitle(
                            ChatColor.GOLD + "You selected",
                            ChatColor.YELLOW + "Rogue!", 10, 40, 10);
                }
                break;
            case "warrior":
                color = RED;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Left-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Left-Click " + ChatColor.GRAY + "to cast slam!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_RED + "You selected",
                            ChatColor.RED + "Warrior!", 10, 40, 10);
                }
                break;
        }
        if (!isTutorial) ClassUtil.launchFirework(player, color);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }

    /**
     * Setup some basic cache info for players.
     *
     * @param player    to set up cache for
     * @param className name of class
     * @param jedis     the jedis resource
     */
    public static void writeClassDataToRedis(Player player, String className, Jedis jedis) {
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(HealthUtils.getBaseHealth());
        player.setHealthScale(HealthUtils.getHeartAmount());
        player.setLevel(0);
        player.setExp(0);
        ClassData classData = new ClassData(player.getUniqueId(), ClassEnum.getFromName(className), 0, 0);
        RunicCoreAPI.setRedisValues(player, classData.toMap(), jedis);
    }
}
