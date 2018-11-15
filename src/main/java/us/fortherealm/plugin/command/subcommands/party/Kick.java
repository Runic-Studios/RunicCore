package us.fortherealm.plugin.command.subcommands.party;

import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;
import pl.kacperduras.protocoltab.ProtocolTabAPI;
import pl.kacperduras.protocoltab.manager.ProtocolTab;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.command.util.TabCompleteUtil;
import us.fortherealm.plugin.nametags.NameTagChanger;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.parties.PartyDisconnect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Kick implements SubCommand {
	
	private PartySC party;
	private Plugin plugin = Main.getInstance();
    private NameTagChanger nameTagChanger = new NameTagChanger();
	
	public Kick(PartySC party) {
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
		
		if(party == null) {
			sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must be in a party to do this!");
			return;
		}

        // if the sender does not specify a player
        if (args.length == 1) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must specify a player with "
                            + ChatColor.YELLOW + "/party kick <player>");
            return;
        }
		
		if(!party.getLeader().equals(sender.getUniqueId())) {
			sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must be the party leader to do this!");
			return;
		}
		
		Player target = Bukkit.getPlayer(args[1]);
		
		if(target == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "Player "
                            + ChatColor.WHITE + args[1]
                            + ChatColor.RED + " not found.");
			return;
		}
		
		if(target.getUniqueId().equals(sender.getUniqueId())) {
			party.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You can't kick yourself from the party!");
			return;
		}

        String targetName = plugin.getConfig().get(target.getUniqueId() + ".info.name").toString();
		String senderName = plugin.getConfig().get(sender.getUniqueId() + ".info.name").toString();
		
		sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.RED + "You kicked "
                        + ChatColor.WHITE + targetName
                        + ChatColor.RED + " from your party!");

		// remove the player from the party and party array
        party.removeMember(target.getUniqueId());
        party.getPartyNames().remove(targetName);

        // reset the party member's name colors for the kicked player
        PartyDisconnect.updatePartyNames(party, target, plugin, nameTagChanger);

        target.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.WHITE + senderName
                        + ChatColor.RED + " kicked you from the party!");

		party.sendMembersMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.WHITE + senderName
                        + ChatColor.RED + " kicked "
                        + ChatColor.WHITE + targetName
                        + ChatColor.RED + " from the party!");

        // update all tablists and name colors for party members
        ProtocolTabAPI.getTablist(target).setSlot(40, "  &a&n Party (" + 0 + ") &r");

        for (int i = 41; i < 60; i++) {
            ProtocolTabAPI.getTablist(target).setSlot(i, ProtocolTab.BLANK_TEXT);
        }
        ProtocolTabAPI.getTablist(target).update();

        // update the player list
        int partyCount = party.getPartySize();
        PartyDisconnect.updatePartyList(party, partyCount);

        // sets the player's name color to RED if outlaw is enabled
        // delay by 0.5s in case the player's outlaw data is null
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getConfig().getBoolean(target.getUniqueId() + ".outlaw.enabled", true)) {
                    nameTagChanger.changeNameGlobal(target, ChatColor.RED + targetName);
                } else {
                    nameTagChanger.changeNameGlobal(target, ChatColor.WHITE + targetName);
                }
            }
        }.runTaskLater(plugin, 10);
	}

    @Override
	public String permissionLabel() {
		return null;
	}

	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
    }
}
