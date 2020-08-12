package com.runicrealms.plugin.player.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.events.RunicExpEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ClassExpCMD implements SubCommand {

    private RunicGiveSC giveItemSC;

    public ClassExpCMD(RunicGiveSC giveItemSC) {
        this.giveItemSC = giveItemSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        // runicgive exp [player] [amount] [x] [y] [z] [levelOfMob]
        // runicgive exp [player] [amount] [uuidOfMob] [mobLevel]
        // runicgive exp [player] [amount] [quest]
        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;
        int exp = Integer.parseInt(args[2]);

        // skip all other calculations for quest exp
        if (args.length == 4) {
            RunicExpEvent e = new RunicExpEvent(exp, pl, RunicExpEvent.RunicExpSource.QUEST, 0, null);
            Bukkit.getPluginManager().callEvent(e);
            return;
        }

        // if the player doesn't have a party or they're in there by themself, give them regular exp.
        if (RunicCore.getPartyManager().getPlayerParty(pl) == null
                || RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).getSize() < 2) {
            if (args.length != 7) {
                RunicExpEvent e = new RunicExpEvent(exp, pl, RunicExpEvent.RunicExpSource.DUNGEON, 0, null);
                Bukkit.getPluginManager().callEvent(e);
            } else {
                int mobLv = Integer.parseInt(args[6]);
                Location loc = new Location(pl.getWorld(), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                RunicExpEvent e = new RunicExpEvent(exp, pl, RunicExpEvent.RunicExpSource.MOB, mobLv, loc);
                Bukkit.getPluginManager().callEvent(e);
            }

        // otherwise, apply party exp bonus (now calculated in listener)
        } else {
            if (args.length == 7) {
                Location loc = new Location(pl.getWorld(), Double.parseDouble(args[3]), Double.parseDouble(args[4]), Double.parseDouble(args[5]));
                RunicExpEvent e = new RunicExpEvent(exp, pl, RunicExpEvent.RunicExpSource.MOB, Integer.parseInt(args[6]), loc);
                Bukkit.getPluginManager().callEvent(e);
            } else {
                RunicExpEvent e = new RunicExpEvent(exp, pl, RunicExpEvent.RunicExpSource.MOB, Integer.parseInt(args[6]), null);
                Bukkit.getPluginManager().callEvent(e);
            }
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if (args.length == 3 || args.length == 4 || args.length == 7) {
            this.onConsoleCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /runicgive exp [player] [amount] ([x] [y] [z]) [level]");
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /runicgive exp [player] [amount] (quest)");
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
    }

    @Override
    public String permissionLabel() {
        return "runic.generateitem";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
