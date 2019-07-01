package com.runicrealms.plugin.player.commands;

import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.List;
import java.util.Objects;

public class SetLevelCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = RunicCore.getInstance();

    public SetLevelCMD(SetSC set) {
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
            sender.sendMessage(ChatColor.RED + "Correct usage: /set level [level] or /set level [player] [level]");
        } else if (args.length == 2) {
            setPlayerLevel(sender, Integer.parseInt(args[1]));
        } else if (args.length == 3) {
            setPlayerLevel(Objects.requireNonNull(Bukkit.getPlayer(args[1])), Integer.parseInt(args[2]));
        }
    }

    private void setPlayerLevel(Player sender, int level) {
        int expAtLevel = PlayerLevelUtil.calculateTotalExp(level) + 1;
        int expectedLv = PlayerLevelUtil.calculateExpectedLv(expAtLevel);
        sender.setLevel(0);
        RunicCore.getInstance().getConfig().set(sender.getUniqueId() + ".info.class.exp", 0);
        PlayerLevelUtil.giveExperience(sender, expAtLevel);
        RunicCore.getInstance().getConfig().set(sender.getUniqueId() + ".info.class.level", expectedLv);
        // ----------------------
        // IMPORTANT: You can't set the exp to 0 here. It must be the expected experience at the profession level!
        // ----------------------
        RunicCore.getInstance().getConfig().set(sender.getUniqueId() + ".info.class.exp", expAtLevel);
        RunicCore.getInstance().saveConfig();
        RunicCore.getInstance().reloadConfig();
    }

    @Override
    public String permissionLabel() {
        return "set.level";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
