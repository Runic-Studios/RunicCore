package us.fortherealm.plugin.skills.util;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.scoreboard.ScoreboardHandler;
import us.fortherealm.plugin.skills.formats.HelixParticleFrame;

public class HealUtil  {

    private ScoreboardHandler sbh = new ScoreboardHandler();

    @SuppressWarnings("deprecation")
    public void healPlayer(double healAmt, Player player, String sourceStr) {

        double newHP = player.getHealth() + healAmt;
        double difference = player.getMaxHealth() - player.getHealth();

        // if they are missing less than healAmt
        if (newHP > player.getMaxHealth()) {
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
            player.sendMessage(ChatColor.GREEN + "+" + (int) healAmt + " ❤" + sourceStr);
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1);
        }

        // update scoreboard health displays
        new BukkitRunnable() {
            @Override
            public void run() {
                sbh.updateSideHealth(player);
                sbh.updateHealthbar(player);
            }
        }.runTaskLater(Main.getInstance(), 1);
    }
}

