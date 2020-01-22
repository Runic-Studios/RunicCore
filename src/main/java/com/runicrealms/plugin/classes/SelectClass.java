package com.runicrealms.plugin.classes;

import com.runicrealms.plugin.classes.utilities.ClassUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.HealthUtils;

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

//    // todo: make this its own thing
//    public static void setupHearthstone(Player pl, String location) {
//        location = location.replace("_", " ");
//        ItemStack hearthstone = new ItemStack(Material.CLAY_BALL);
//        hearthstone = AttributeUtil.addCustomStat(hearthstone, "location", location);
//        hearthstone = AttributeUtil.addCustomStat(hearthstone, "soulbound", "true");
//        LoreGenerator.generateHearthstoneLore(hearthstone);
//        pl.getInventory().setItem(2, hearthstone);
//    }

    public static void setConfig(Player player, String className) {
        HealthUtils.setBaseHealth(player);
        HealthUtils.setHeartDisplay(player);
        player.setLevel(0);
        player.setExp(0);
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.class.name", className);
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.class.level", 0);
        RunicCore.getInstance().getConfig().set(player.getUniqueId() + ".info.class.exp", 0);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
    }
}
