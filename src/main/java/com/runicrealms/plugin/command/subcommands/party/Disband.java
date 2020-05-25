package com.runicrealms.plugin.command.subcommands.party;

import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.parties.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.ArrayList;
import java.util.List;

public class Disband implements SubCommand {
	
	private PartySC party;
	
	public Disband(PartySC party) {
		this.party = party;
	}
	
	@Override
	public void onConsoleCommand(CommandSender sender, String[] args) {
	
	}
	
	@Override
	public void onOPCommand(Player sender, String[] args) {

        if(args.length == 1) {
            sender.sendMessage
                    (ChatColor.DARK_RED + "Party Admin "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must specify a player with "
                            + ChatColor.YELLOW + "/party disband <player>");
            return;
        }

	    Player target = Bukkit.getPlayer(args[1]);

        if(target == null) {
            sender.sendMessage
                    (ChatColor.DARK_RED + "Party Admin "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "Player "
                            + ChatColor.WHITE + args[1]
                            + ChatColor.RED + " not found.");
            return;
        }

        String storedTargetName = target.getName();

		if(!args[1].equalsIgnoreCase("admin"))
			this.onUserCommand(sender, args);

		Party targetParty = RunicCore.getPartyManager().getPlayerParty(target);

		if(targetParty != null) {

		    ArrayList<Player> members = targetParty.getPlayerMembers();

            // reset tablist for leader and members, inform them of disband
            for (Player member : targetParty.getPlayerMembers()) {

                member.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.RED + "Your party has been disbanded by an admin!");

            }

            // reset the party member's name colors for the kicked player
            PartyDisconnect.updatePartyNames(targetParty, target);

            // disband the party
			RunicCore.getPartyManager().disbandParty(targetParty);

            // update the tablist
            for (Player member : members) {
                RunicCore.getTabListManager().setupTab(member);
            }

			sender.sendMessage
                    (ChatColor.DARK_RED + "Party Admin "
                            + ChatColor.GOLD + "» "
                            + ChatColor.GREEN + "You have forcefully disbanded "
                            + ChatColor.WHITE + storedTargetName
                            + ChatColor.GREEN + "'s party!");

		} else {
			sender.sendMessage
                    (ChatColor.DARK_RED + "Party Admin "
                            + ChatColor.GOLD + "» "
                            + ChatColor.WHITE + storedTargetName
                            + ChatColor.RED + " is not in a party!");
		}
	}
	
	@Override
	public void onUserCommand(Player sender, String[] args) {
		Party party = RunicCore.getPartyManager().getPlayerParty(sender);
	}
	
	@Override
	public String permissionLabel() {
		return "party.disband";
	}
	
	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return TabCompleteUtil.getPlayers(commandSender, strings, RunicCore.getInstance());
	}
}
