package com.runicrealms.plugin.commands.admin;

import com.runicrealms.plugin.classes.utilities.ClassUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class FireworkCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // runicfirework <target.uuid>
        if (!sender.isOp())
            return true;
        try {

            Player pl = Bukkit.getPlayer(UUID.fromString(args[0]));
            if (pl == null)
                return true;
            pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
            ClassUtil.launchFirework(pl, Color.GREEN);
        } catch (Exception e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "Error");
            return true;
        }
        return true;
    }
}
