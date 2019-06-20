package com.runicrealms.plugin.command.subcommands.set;

import com.runicrealms.plugin.professions.ProfGUI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;
import com.runicrealms.plugin.player.commands.SetSC;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;

import java.util.List;

public class SetProfCMD implements SubCommand {

    private SetSC set;
    private Plugin plugin = RunicCore.getInstance();
    private ScoreboardHandler sbh = RunicCore.getScoreboardHandler();

    public SetProfCMD(SetSC set) {
        this.set = set;
    }


    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

        // if the sender does not specify the correct arguments
        setProf(sender, args);
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onUserCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {

        // if the sender does not specify the correct arguments
        setProf(sender, args);
    }

    @Override
    public String permissionLabel() {
        return "set.prof";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }

    private void setProf(CommandSender sender, String[] args) {
        if (args.length != 2 && args.length != 3) {
            sender.sendMessage(ChatColor.RED + "Correct usage: /set prof {player} or /set prof {player} {prof}");
            return;
        }

        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;

        if (args.length == 2) {
            ProfGUI.PROF_SELECTION.open(pl);
        } else {

            String profStr = args[2].toLowerCase();
            if (!(profStr.equals("alchemist")
                    || profStr.equals("blacksmith")
                    || profStr.equals("jeweler")
                    || profStr.equals("leatherworker")
                    || profStr.equals("tailor"))) {

                sender.sendMessage(ChatColor.RED
                        + "Available classes: alchemist, blacksmith, jeweler, leatherworker, tailor");
                return;
            }

            String formattedStr = profStr.substring(0, 1).toUpperCase() + profStr.substring(1);

            ProfGUI.setConfig(pl, formattedStr);
            sbh.updatePlayerInfo(pl);
            sbh.updateSideInfo(pl);
        }
    }
}
