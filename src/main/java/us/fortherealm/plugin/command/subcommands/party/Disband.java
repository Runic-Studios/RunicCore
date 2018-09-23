package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Party;

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
		String targetPlayer = args[1];
		Player target = Bukkit.getPlayer(targetPlayer);
															// label args[0] args[1] args[2]
		if(!args[1].equalsIgnoreCase("admin"))  // /party disband admin <player in party>
			this.onUserCommand(sender, args);
		
		if (target == null) {
			sender.sendMessage(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Party Admin " + ChatColor.GRAY.toString() + ChatColor.BOLD + "> " +
					ChatColor.RED + "Player " + ChatColor.GOLD + args[2] + ChatColor.RED + " not found!");
			return;
		}
		
		Party targetParty = Main.getPartyManager().getPlayerParty(target);
		if(targetParty != null) {
			Main.getPartyManager().disbandParty(targetParty);
			sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Party Admin " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.RED + "You have forcefully disbanded " + ChatColor.GOLD + target.getName() + ChatColor.RED + "'s party!");
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "Party Admin " + ChatColor.GRAY + "" + ChatColor.BOLD + "> " +
					ChatColor.GOLD + target.getName() + ChatColor.RED + " is not in a party!");
		}
	}
	
	@Override
	public void onUserCommand(Player sender, String[] args) {
		
		Party party = Main.getPartyManager().getPlayerParty(sender);
		
		
	}
	
	@Override
	public String permissionLabel() {
		return "party.disband";
	}
	
	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
		return null;
	}
}
