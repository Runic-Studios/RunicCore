package com.runicrealms.plugin.player.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.supercommands.SuperCommand;

public class Mana extends SuperCommand {

    public Mana() {
        super("player.set.mana");
    }

    @Override
    public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player pl = (Player) sender;
            int maxMana = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.maxMana");
            RunicCore.getManaManager().getCurrentManaList().put(pl.getUniqueId(), maxMana);
            RunicCore.getScoreboardHandler().updateSideInfo(pl);
            pl.sendMessage(ChatColor.AQUA + "You've restored your mana!");
        }
    }
}
