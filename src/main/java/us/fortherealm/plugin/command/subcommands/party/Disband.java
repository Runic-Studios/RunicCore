package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.command.util.TabCompleteUtil;
import us.fortherealm.plugin.nametags.NameTagChanger;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.parties.PartyDisconnect;

import java.util.ArrayList;
import java.util.List;

public class Disband implements SubCommand {
	
	private PartySC party;
	private NameTagChanger nameTagChanger = new NameTagChanger();
	private Plugin plugin = FTRCore.getInstance();
	
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

        String storedTargetName = plugin.getConfig().get(target.getUniqueId() + ".info.name").toString();

		if(!args[1].equalsIgnoreCase("admin"))
			this.onUserCommand(sender, args);

		Party targetParty = FTRCore.getPartyManager().getPlayerParty(target);

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
            PartyDisconnect.updatePartyNames(targetParty, target, plugin, nameTagChanger);
            for (Player member : targetParty.getPlayerMembers()) {
                PartyDisconnect.updatePartyNames(targetParty, member, plugin, nameTagChanger);
            }

            // disband the party
			FTRCore.getPartyManager().disbandParty(targetParty);

            // update the tablist
            for (Player member : members) {
                FTRCore.getTabListManager().setupTab(member);
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
		Party party = FTRCore.getPartyManager().getPlayerParty(sender);
	}
	
	@Override
	public String permissionLabel() {
		return "party.disband";
	}
	
	@Override
	public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        return TabCompleteUtil.getPlayers(commandSender, strings, plugin);
	}
}
