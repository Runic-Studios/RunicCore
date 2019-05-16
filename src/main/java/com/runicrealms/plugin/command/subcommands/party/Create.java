package com.runicrealms.plugin.command.subcommands.party;

import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.parties.Party;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;

import java.util.List;

public class Create implements SubCommand {

	private PartySC party;
	private Plugin plugin = RunicCore.getInstance();

	public Create(PartySC party) {
		this.party = party;
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

		Party party = RunicCore.getPartyManager().getPlayerParty(sender);

		// player's can't make a party if they're in one
		if(party != null) {
			sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You are already in a party!");
			return;
		}

		// create the party
		RunicCore.getPartyManager().addParty(new Party(sender.getUniqueId()));
		sender.playSound(sender.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);

		// inform the sender
		sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.GREEN + "You created a party!");

		// update tab
        RunicCore.getTabListManager().setupTab(sender);
	}

	@Override
	public String permissionLabel() {
		return "party.create";
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
