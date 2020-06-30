package com.runicrealms.plugin.command;

import com.runicrealms.plugin.events.MobDamageEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RunicDamage implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // runicdamage <caster.uuid> <target.name> <amount>
        if (!sender.isOp())
            return true;

        try {
            Entity caster = Bukkit.getEntity(UUID.fromString(args[0]));
            Player pl = Bukkit.getPlayer(args[1]);
            int amount = Integer.parseInt(args[2]);
            if (caster == null)
                return true;
            if (pl == null)
                return true;
            if (pl.getGameMode() != GameMode.SURVIVAL)
                return true;

            MobDamageEvent e = new MobDamageEvent(amount, caster, pl, false);
            Bukkit.getPluginManager().callEvent(e);
            return true;

        } catch (Exception e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "RunicDamage improperly configured. Please check logs!");
            return true;
        }
    }
}
