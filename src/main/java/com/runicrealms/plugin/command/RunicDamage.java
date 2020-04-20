package com.runicrealms.plugin.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.UUID;

public class RunicDamage implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // runicdamage <caster.uuid> <target.name> <amount>
        if (!sender.isOp()) return false;

        Entity caster = Bukkit.getEntity(UUID.fromString(args[0]));
        Player pl = Bukkit.getPlayer(args[1]);
        int amount = Integer.parseInt(args[2]);
        if (caster == null) return false;
        if (pl == null) return false;

        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(caster, pl, EntityDamageEvent.DamageCause.ENTITY_ATTACK, amount);
        Bukkit.getPluginManager().callEvent(e);

        return !e.isCancelled();
    }

}
