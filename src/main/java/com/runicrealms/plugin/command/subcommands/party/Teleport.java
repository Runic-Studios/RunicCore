package com.runicrealms.plugin.command.subcommands.party;

import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.parties.Party;
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

public class Teleport implements SubCommand {

    private PartySC party;
    private Plugin plugin = RunicCore.getInstance();

    public Teleport(PartySC party) {
        this.party = party;
    }

    @Override
    public void onConsoleCommand(CommandSender sender, String[] args) {

//        if (!sender.isOp()) {
//            sender.sendMessage(ChatColor.RED + "You do not have access to this command.");
//            return;
//        }

        // if the sender does not specify a player
        if (args.length == 1) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must specify a player with "
                            + ChatColor.YELLOW + "/party teleport <player>");
            return;
        }

        // party teleport <player>
        Player pl = Bukkit.getPlayer(args[1]);
        if (pl == null) return;

        if (RunicCore.getPartyManager().getPlayerParty(pl) == null) return;
        Party party = RunicCore.getPartyManager().getPlayerParty(pl);

        for (Player member : party.getPlayerMembers()) {
            if (member == pl) continue;
            member.teleport(pl.getLocation().add(0, 1, 0));
        }
    }

    @Override
    public void onOPCommand(Player sender, String[] args) {
        this.onConsoleCommand(sender, args);
    }

    @Override
    public void onUserCommand(Player sender, String[] args) {
        sender.sendMessage(ChatColor.RED + "You don't have access to that command.");
    }

    @Override
    public String permissionLabel() {
        return "party.invite";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
