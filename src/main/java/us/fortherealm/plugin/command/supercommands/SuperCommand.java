package us.fortherealm.plugin.command.supercommands;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.util.CommandsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public abstract class SuperCommand implements CommandExecutor, TabCompleter {
	
	private HashMap<List<String>, SubCommand> subCommandHashMap = new HashMap<>();
	private final String permissionLabel;
	private final CommandsUtil commandHelper;
	
	public SuperCommand(String permissionLabel) {
		this(
				new CommandsUtil(
					ChatColor.RED.toString() + "You do not have permission to use this command.",
					ChatColor.RED.toString() + "Command not found."
				),
				permissionLabel
		);
	}
	
	public SuperCommand(CommandsUtil commandHelper, String permissionLabel) {
		this.commandHelper = commandHelper;
		this.permissionLabel = permissionLabel;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!commandHelper.senderHasPerms(sender, true, permissionLabel))
			return true;
		
		if(args.length == 0) {
			executeBasicCommand(sender, cmd, label, args);
			return true;
		}
		
		executeSubCommand(sender, cmd, label, args);
		
		return true;
	}
	
	abstract public void executeBasicCommand(CommandSender sender, Command cmd, String label, String[] args);
	
	public void executeSubCommand(CommandSender sender, Command cmd, String label, String[] args) {
		SubCommand subCommand = findAvailableSubCommandFromString(args[0], sender,true);
		
		if(subCommand == null) {
			sender.sendMessage(commandHelper.getNoCommandMessage());
			return;
		}
		
		if(sender instanceof ConsoleCommandSender)
		{
			subCommand.onConsoleCommand(sender, args);
		}
		else
		{
			Player player = (Player) sender;
			
			if(player.isOp() || player.hasPermission("ftr.staffmode"))
			{
				subCommand.onOPCommand(player, args);
			}
			else
			{
				subCommand.onUserCommand(player, args);
			}
		}
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		CommandsUtil cmdHelper = commandHelper;
		if(args.length == 0)
			return getAvailableSubCommandAliasList(sender);
		
		if(args.length > 1) {
			SubCommand possibleCommand = findAvailableSubCommandFromString(args[0], sender, false);
			if(possibleCommand.permissionLabel() != null &&
					!(commandHelper.senderHasPerms(sender, false, possibleCommand.permissionLabel())))
				return null;
			return possibleCommand.onTabComplete(sender, cmd, label, args);
		}
		
		List<String> refinedList = new ArrayList<>();
		for(String item : getAvailableSubCommandAliasList(sender)) {
			if(item.startsWith(args[0].toLowerCase()))
				refinedList.add(item);
		}
		return refinedList;
	}
	
	public void addCommand(List<String> commandNames, SubCommand subCommand) {
		subCommandHashMap.put(commandNames, subCommand);
	}
	
	private SubCommand findAvailableSubCommandFromString(String arg, CommandSender sender, boolean shouldFindIntendedSubCommand) {
		SubCommand possibleSubCommand = null;
		for(List<String> subCommandNames : subCommandHashMap.keySet()) {
			if(subCommandHashMap.get(subCommandNames).permissionLabel() != null &&
					!(commandHelper.senderHasPerms(sender, false,
							subCommandHashMap.get(subCommandNames).permissionLabel())))
				continue;
			for(String subCommandName : subCommandNames) {
				if (subCommandName.toLowerCase().equals(arg.toLowerCase())) {
					return subCommandHashMap.get(subCommandNames);
				}
			}
		}
		
		if(shouldFindIntendedSubCommand)
			return findIntendedSubCommandFromString(arg, sender);
		
		return null;
	}
	
	private SubCommand findIntendedSubCommandFromString(String arg, CommandSender sender) {
		String intendedString = null;
		for (String possibleAlias : getAvailableSubCommandAliasList(sender)) {
			if (possibleAlias.toLowerCase().startsWith(arg.toLowerCase())) {
				if (intendedString != null)
					return null;
				intendedString = possibleAlias;
			}
		}
		if (intendedString == null)
			return null;
		return findAvailableSubCommandFromString(intendedString, sender, false);
	}
	
	private List<String> getAvailableSubCommandAliasList(CommandSender sender) {
		List<String> availableAliasList = new ArrayList<>();
		for(List<String> subCommandNames : subCommandHashMap.keySet()) {
			if(subCommandHashMap.get(subCommandNames).permissionLabel() != null) {
				if(!(commandHelper.senderHasPerms(sender, subCommandHashMap.get(subCommandNames).permissionLabel()))) {
					continue;
				}
			}
			for(String item : subCommandNames)
				availableAliasList.add(item.toLowerCase());
		}
		Collections.sort(availableAliasList);
		return availableAliasList;
	}
	
	private List<String> getSubCommandAliasList() {
		List<String> subCommands = new ArrayList<>();
		for(List<String> subCommandKeys : subCommandHashMap.keySet())
			for(String item : subCommandKeys)
				subCommands.add(item.toLowerCase());
		Collections.sort(subCommands);
		return subCommands;
	}
	
	
}
