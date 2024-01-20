package com.runicrealms.plugin.commands.admin;

import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.utilities.DamageUtil;
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
            Player player = Bukkit.getPlayer(args[1]);
            int amount = Integer.parseInt(args[2]);
            if (caster == null)
                return true;
            if (player == null)
                return true;
            if (player.getGameMode() == GameMode.CREATIVE)
                return true;

            MobDamageEvent mobDamageEvent = new MobDamageEvent(amount, caster, player, false);
            Bukkit.getPluginManager().callEvent(mobDamageEvent);
            if (!mobDamageEvent.isCancelled()) {
                DamageUtil.damageEntityMob(mobDamageEvent.getAmount(), mobDamageEvent.getVictim(), mobDamageEvent.getMob(), mobDamageEvent.shouldApplyMechanics());
            }
            return true;

        } catch (Exception e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "RunicDamage improperly configured. Please check logs!");
            return true;
        }
    }
}
