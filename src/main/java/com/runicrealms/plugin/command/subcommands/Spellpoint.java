package com.runicrealms.plugin.command.subcommands;

import com.runicrealms.plugin.command.supercommands.SpellpointSC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;

import java.util.List;

public class Spellpoint implements SubCommand {

    private SpellpointSC spellpointSC;
    private Plugin plugin = RunicCore.getInstance();
    private ScoreboardHandler sbh = RunicCore.getScoreboardHandler();

    public Spellpoint(SpellpointSC spellpointSC) {
        this.spellpointSC = spellpointSC;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

        // if the sender does not specify the correct arguments
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /spellpoint give [player]");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);

        // give player 1 spellpoint
        if (pl != null) {
            int spellpoints = RunicCore.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.spellpoints");
            RunicCore.getInstance().getConfig().set(pl.getUniqueId() + ".info.spellpoints", spellpoints+1);
            RunicCore.getInstance().saveConfig();
            RunicCore.getInstance().reloadConfig();
            pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You've earned a spellpoint!");
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onConsoleCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
        this.onConsoleCommand(sender, args);
    }

    @Override
    public String permissionLabel() {
        return "ftrcore.spellpoint";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
