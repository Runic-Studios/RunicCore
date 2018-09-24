package us.fortherealm.plugin.skills.util;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class HealUtil  {

    public static void healPlayer(double healAmt, Player player, String sourceStr) {

        double newHP = player.getHealth() + healAmt;
        double difference = player.getMaxHealth() - player.getHealth();

        if (newHP > player.getMaxHealth()) { // if they are missing less than healAmt
            player.getWorld().spigot().playEffect(player.getEyeLocation(), Effect.HEART, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 5, 16);
            player.setHealth(player.getMaxHealth());
            if (difference != (int) difference) {
                player.sendMessage(ChatColor.GREEN + "+" + ((int) difference + 1) + " ❤" + sourceStr);
            }
            if (difference == (int) difference) {
                player.sendMessage(ChatColor.GREEN + "+" + ((int) difference) + " ❤" + sourceStr);
            }
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
        } else {
            player.setHealth(newHP);
            player.getWorld().spigot().playEffect(player.getEyeLocation(), Effect.HEART, 0, 0, 0.3F, 0.3F, 0.3F, 0.01F, 5, 16);
            player.sendMessage(ChatColor.GREEN + "+" + (int) healAmt + " ❤" + sourceStr);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
        }
    }
}

