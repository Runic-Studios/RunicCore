package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.nametags.NameTagChanger;
import us.fortherealm.plugin.parties.PartyDisconnect;

import java.util.List;
import java.util.UUID;

public class Leave implements SubCommand {

	private PartySC party;
    private Plugin plugin = FTRCore.getInstance();
	private NameTagChanger nameTagChanger = new NameTagChanger();

	public Leave(PartySC party) {
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

		Party party = FTRCore.getPartyManager().getPlayerParty(sender);

		// if the sender is not in a party
		if(party == null) {
			sender.sendMessage
					(ChatColor.DARK_GREEN + "Party "
							+ ChatColor.GOLD + "» "
                            + ChatColor.RED + "You are not in a party!");
			return;
		}

        // grab the player's stored name
        String senderNameToString = plugin.getConfig().get(sender.getUniqueId() + ".info.name").toString();

        // leaver is removed from the party
        party.removeMember(sender.getUniqueId());
        sender.playSound(sender.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 1);

        // reset the party member's name colors for the leaver
        PartyDisconnect.updatePartyNames(party, sender, plugin, nameTagChanger);

        // if the new member count is less than 1, just disband the party
		if (party.getPartySize() < 1) {
            FTRCore.getPartyManager().disbandParty(party);
            FTRCore.getTabListManager().setupTab(sender);
        } else {

            // if the party leaver is not the leader
            if(!party.getLeader().equals(sender.getUniqueId())) {
                party.removeMember(sender.getUniqueId());
                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + senderNameToString + " &chas left the party!");
            } else {

                // party leader is set to whoever is now in position [0]
                party.setLeader(party.getMemberUUID(0));

                // grab the new Player newLeader from their uuid in the party array
                Player newLeader = Bukkit.getPlayer(UUID.fromString(party.getLeader().toString()));

                // grab the new leader's stored name
                Object storedNameLead = plugin.getConfig().get(newLeader.getUniqueId() + ".info.name");
                String newLeadNameToString = storedNameLead.toString();

                // inform members of leader change
                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + senderNameToString
                                + ChatColor.RED + " left the party. "
                                + ChatColor.WHITE + newLeadNameToString
                                + ChatColor.GREEN + " is now the party leader!");
            }
        }

        // send leaver message
        sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.RED + "You left your party!");

        // remove the leaver from the party array
        party.getPartyNames().remove(senderNameToString);

        // update the tablist
        for (Player member : party.getPlayerMembers()) {
            FTRCore.getTabListManager().setupTab(member);
        }
        FTRCore.getTabListManager().setupTab(sender);

        // sets the player's name color to RED if outlaw is enabled
        // delay by 0.5s in case the player's outlaw data is null
        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.getConfig().getBoolean(sender.getUniqueId() + ".outlaw.enabled", true)) {
                    nameTagChanger.changeNameGlobal(sender, ChatColor.RED + senderNameToString);
                } else {
                    nameTagChanger.changeNameGlobal(sender, ChatColor.WHITE + senderNameToString);
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
		return null;
	}
}
