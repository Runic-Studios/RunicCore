package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Invite;
import us.fortherealm.plugin.parties.Party;

import java.util.List;

public class Join implements SubCommand {
	
	private PartySC party;
	
	public Join(PartySC party) {
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
		Party party = Main.getPartyManager().getPlayerParty(sender);
		
		if(party != null) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "You are already in a party");
			return;
		}
		
		Invite invite = Main.getPartyManager().getActiveInvite(sender);
		if(invite != null) {
			sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "You don't have an invite to a party!");
			return;
		}
		
		sender.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
				ChatColor.GREEN + "You joined " + ChatColor.GOLD + invite.getInviter() + ChatColor.GREEN + "'s party!");
		invite.getParty().sendMemberMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Party " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
				ChatColor.GOLD +  sender.getName() + ChatColor.GREEN + " has joined the party!");
		invite.getParty().addMember(sender.getUniqueId());
		Main.getPartyManager().removeInvite(invite);
		
	}
	
	@Override
	public String permissionLabel() {
		return null;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
