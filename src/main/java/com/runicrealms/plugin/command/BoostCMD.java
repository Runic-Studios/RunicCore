package com.runicrealms.plugin.command;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.utilities.ClassUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class BoostCMD implements CommandExecutor {

    private static double COMBAT_EXPERIENCE_BOOST = 0;

    /**
     it needs to generate the name and stats of the item and choose from a list of durabilities. (prob 0 or 1 rn)
     it should use the custom item tagging class as well
     */
    public boolean onCommand(CommandSender sender, Command cmd, String lb, String[] args) {

        /*
        /boost [player] [percent] [duration in seconds]
         */
        if (args.length != 3) return false;
        Player pl = Bukkit.getPlayer(args[0]);
        if (pl == null) return false;
        double percent = Double.parseDouble(args[1]);
        int duration = Integer.parseInt(args[2]);

        COMBAT_EXPERIENCE_BOOST = percent;
        ClassUtil.launchFirework(pl, Color.FUCHSIA);
        for (Player online : RunicCore.getCacheManager().getLoadedPlayers()) {
            online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        }

        Bukkit.broadcastMessage
                (ChatColor.DARK_PURPLE + "[Runic Realms] "
                        + ChatColor.WHITE + pl.getName()
                        + ChatColor.LIGHT_PURPLE + " has activated a "
                        + (int) percent + "% combat experience boost for "
                        + duration/3600 +"h!");

        new BukkitRunnable() {
            @Override
            public void run() {
                COMBAT_EXPERIENCE_BOOST = 0;
                Bukkit.broadcastMessage
                        (ChatColor.DARK_PURPLE + "[Runic Realms] "
                                + ChatColor.WHITE + pl.getName() + "'s"
                                + ChatColor.LIGHT_PURPLE + " global combat experience boost has ended!");
            }
        }.runTaskLaterAsynchronously(RunicCore.getInstance(), 20L*duration);
        return true;
    }

    public static double getCombatExperienceBoost() {
        return COMBAT_EXPERIENCE_BOOST;
    }
}
