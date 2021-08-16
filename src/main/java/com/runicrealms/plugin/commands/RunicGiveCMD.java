package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Subcommand;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("runicgive")
public class RunicGiveCMD extends BaseCommand {

    // runicgive exp [player] [amount] [x] [y] [z] [mobLevel]
    // runicgive exp [player] [amount] [uuidOfMob] [mobLevel]
    // runicgive exp [player] [amount] [quest]

    @Subcommand("exp")
    @CommandCompletion("@players")
    @Conditions("is-console-or-op")
    public void onCommandExp(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments.");
            return;
        }
        Player pl = Bukkit.getPlayer(args[0]);
        if (pl == null) return;
        int exp = Integer.parseInt(args[1]);

        // skip all other calculations for quest exp
        if (args.length == 3) {
            RunicExpEvent e = new RunicExpEvent(exp, exp, pl, RunicExpEvent.RunicExpSource.QUEST, 0, null);
            Bukkit.getPluginManager().callEvent(e);
            return;
        }

        // if the player doesn't have a party, or they're in by themselves, give them regular exp.
        if (RunicCore.getPartyManager().getPlayerParty(pl) == null
                || RunicCore.getPartyManager().getPlayerParty(pl) != null
                && RunicCore.getPartyManager().getPlayerParty(pl).getSize() < 2) {
            if (args.length != 6) {
                RunicExpEvent e = new RunicExpEvent(exp, exp, pl, RunicExpEvent.RunicExpSource.DUNGEON, 0, null);
                Bukkit.getPluginManager().callEvent(e);
            } else {
                int mobLv = Integer.parseInt(args[5]);
                Location loc = new Location(pl.getWorld(), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                RunicExpEvent e = new RunicExpEvent(exp, exp, pl, RunicExpEvent.RunicExpSource.MOB, mobLv, loc);
                Bukkit.getPluginManager().callEvent(e);
            }

            // otherwise, apply party exp bonus (now calculated in listener)
        } else {
            if (args.length == 6) {
                Location loc = new Location(pl.getWorld(), Double.parseDouble(args[2]), Double.parseDouble(args[3]), Double.parseDouble(args[4]));
                RunicExpEvent e = new RunicExpEvent(exp, exp, pl, RunicExpEvent.RunicExpSource.MOB, Integer.parseInt(args[5]), loc);
                Bukkit.getPluginManager().callEvent(e);
            } else {
                RunicExpEvent e = new RunicExpEvent(exp, exp, pl, RunicExpEvent.RunicExpSource.MOB, Integer.parseInt(args[5]), null);
                Bukkit.getPluginManager().callEvent(e);
            }
        }
    }

    // runicgive profexp [player] [amount]
    // runicgive profexp [player] [amount] [quest]

    @Subcommand("profexp")
    @Conditions("is-console-or-op")
    public void onCommandProfExp(CommandSender commandSender, String[] args) {
        if (args.length < 1) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments.");
            return;
        }
        Player pl = Bukkit.getPlayer(args[0]);
        if (pl == null) return;

        // skip all other calculations for quest exp
        int exp = Integer.parseInt(args[1]);
        ProfExpUtil.giveExperience(pl, exp, true);

    }
}
