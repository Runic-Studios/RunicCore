package com.runicrealms.plugin.player.commands;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import com.runicrealms.plugin.utilities.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class SetProfLevelCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = RunicCore.getInstance();

    public SetProfLevelCMD(SetSC set) {
        this.set = set;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify a player
        if (args.length == 1) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set proflevel [level] or /set proflevel [player] [level]");
        } else if (args.length == 2) {
            RunicCore.getCacheManager().getPlayerCache(sender.getUniqueId()).setProfLevel(Integer.parseInt(args[1]));
            // ----------------------
            // IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the profession level!
            int expAtLevel = ProfExpUtil.calculateTotalExperience(Integer.parseInt(args[1]));
            // ----------------------
            RunicCore.getCacheManager().getPlayerCache(sender.getUniqueId()).setProfExp(expAtLevel);

            String profName = RunicCore.getCacheManager().getPlayerCache(sender.getUniqueId()).getProfName();
            if (Integer.parseInt(args[1]) == 30) {

                sender.sendMessage("\n");
                sender.playSound(sender.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                ChatUtils.sendCenteredMessage(sender, ChatColor.GREEN + "" + ChatColor.BOLD + "PROFESSION UPGRADE!");
                ChatUtils.sendCenteredMessage(sender, ChatColor.WHITE + "" + ChatColor.BOLD + "You are now a Refined " + profName + "!");
                ChatUtils.sendCenteredMessage(sender, ChatColor.GRAY + "        Your crafted goods have become more powerful!");
                sender.sendMessage("\n");

            } else if (Integer.parseInt(args[1]) == 50) {

                sender.sendMessage("\n");
                sender.playSound(sender.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
                ChatUtils.sendCenteredMessage(sender, ChatColor.GREEN + "" + ChatColor.BOLD + "PROFESSION UPGRADE!");
                ChatUtils.sendCenteredMessage(sender, ChatColor.WHITE + "" + ChatColor.BOLD + "You are now an Artisan " + profName + "!");
                ChatUtils.sendCenteredMessage(sender, ChatColor.GRAY + "        Your crafted goods have become more powerful!");
                sender.sendMessage("\n");

            }

        } else if (args.length == 3) {
            Player player = Bukkit.getPlayer(args[1]);
            if (player == null) return;
            RunicCore.getCacheManager().getPlayerCache(sender.getUniqueId()).setProfLevel(Integer.parseInt(args[1]));
            // ----------------------
            // IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the profession level!
            int expAtLevel = ProfExpUtil.calculateTotalExperience(Integer.parseInt(args[1]));
            // ----------------------
            RunicCore.getCacheManager().getPlayerCache(sender.getUniqueId()).setProfExp(expAtLevel);
        }
    }

    @Override
    public String permissionLabel() {
        return "set.proflevel";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
