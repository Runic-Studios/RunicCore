package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Private;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("vanish")
@CommandPermission("runiccore.vanish")
@Private
public class VanishCMD extends BaseCommand {


    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        if (RunicCore.getVanishAPI().getVanishedPlayers().contains(player)) {
            RunicCore.getVanishAPI().showPlayer(player);
            player.sendMessage(ChatColor.GREEN + "You have reappeared!");
        } else {
            RunicCore.getVanishAPI().hidePlayer(player);
            player.sendMessage(ChatColor.GREEN + "You have vanished in-game, in tab list, and in player count!");
        }
    }

}
