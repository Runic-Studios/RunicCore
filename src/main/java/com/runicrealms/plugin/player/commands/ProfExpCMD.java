package com.runicrealms.plugin.player.commands;

import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.supercommands.RunicGiveSC;
import com.runicrealms.plugin.professions.utilities.ProfExpUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@SuppressWarnings("FieldCanBeLocal")
public class ProfExpCMD implements SubCommand {

    private RunicGiveSC giveItemSC;

    public ProfExpCMD(RunicGiveSC giveItemSC) {
        this.giveItemSC = giveItemSC;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args)  {

        // runicgive profexp [player] [amount]
        // runicgive profexp [player] [amount] [quest]
        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;

        // skip all other calculations for quest exp
        if (args.length == 4) {
            int exp = Integer.parseInt(args[2]);
            ProfExpUtil.giveExperience(pl, exp, true);
            return;
        } else {
            int exp = Integer.parseInt(args[2]);
            ProfExpUtil.giveExperience(pl, exp, true);
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {

        if (args.length == 3 || args.length == 4) {
            this.onConsoleCommand(sender, args);
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /runicgive exp [player] [amount]");
            sender.sendMessage(ChatColor.YELLOW + "Command usage: /runicgive exp [player] [amount] (quest)");
        }
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
    }

    @Override
    public String permissionLabel() {
        return "runic.profexp";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return null;
    }
}
