package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Single;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("tphere")
@CommandPermission("runiccore.tphere")
public class TPHereCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @CommandCompletion("@online")
    public void onCommand(Player player, @Single String targetName) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "That player is not online!");
            return;
        }
        target.teleport(player);
        player.sendMessage(ChatColor.GREEN + "You teleported " + targetName + " to your location!");
    }

}
