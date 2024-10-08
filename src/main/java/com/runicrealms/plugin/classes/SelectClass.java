package com.runicrealms.plugin.classes;

import com.runicrealms.plugin.classes.utilities.ClassUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static org.bukkit.Color.AQUA;
import static org.bukkit.Color.FUCHSIA;
import static org.bukkit.Color.LIME;
import static org.bukkit.Color.RED;
import static org.bukkit.Color.WHITE;
import static org.bukkit.Color.YELLOW;

public class SelectClass {

    // sets the player artifact
    public static void setPlayerClass(Player player, String className, boolean isTutorial) {

        // build class-specific variables
        Color color = WHITE;
        switch (className.toLowerCase()) {
            case "archer" -> {
                color = LIME;
                if (isTutorial) {
                    player.sendTitle(
                            ChatColor.GOLD + "Try",
                            ChatColor.YELLOW + "Sneak + Right-Click!", 10, 100, 10);
                    player.sendMessage(ChatColor.GOLD + "Try " + ChatColor.YELLOW + "Sneak + Right-Click " + ChatColor.GRAY + "to cast rapid fire!");
                } else {
                    player.sendTitle(
                            ChatColor.DARK_GREEN + "You selected",
                            ChatColor.GREEN + "Archer!", 10, 40, 10);
                }
            }
            case "cleric" -> {
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
            }
            case "mage" -> {
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
            }
            case "rogue" -> {
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
            }
            case "warrior" -> {
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
            }
        }
        if (!isTutorial) ClassUtil.launchFirework(player, color);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
    }

}
