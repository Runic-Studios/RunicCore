package us.fortherealm.plugin.util;

import org.bukkit.command.CommandSender;

public class CommandsUtil {
	
	private final String noPermMessage;
	private final String noCommandMessage;
	private final boolean allowOpsAllPerms;
	
	public CommandsUtil(String noPermMessage, String noCommandMessage) {
		this(noPermMessage, noCommandMessage, true);
	}
	
	public CommandsUtil(String noPermMessage, String noCommandMessage, boolean allowOpsAllPerms) {
		this.noPermMessage = noPermMessage;
		this.noCommandMessage = noCommandMessage;
		this.allowOpsAllPerms = allowOpsAllPerms;
	}
	
	public boolean senderHasPerms(CommandSender sender, String... perms) {
		return this.senderHasPerms(sender, false, perms);
	}
	
	public boolean senderHasPerms(CommandSender sender, boolean shouldSendNoPermMessage, String... perms) {
		if(perms == null)
			return true;
		
		if(sender.isOp() && allowOpsAllPerms)
			return true;
		
		boolean hasPerms = true;
		for(String perm : perms) {
			if(sender.hasPermission(perm))
				continue;
			hasPerms = false;
			break;
		}
		
		if(shouldSendNoPermMessage && !hasPerms)
			sender.sendMessage(getNoPermMessage());
		
		return hasPerms;
	}
	
	public String getNoCommandMessage() {
		return noCommandMessage;
	}
	
	public String getNoPermMessage() {
		return noPermMessage;
	}
	
}
