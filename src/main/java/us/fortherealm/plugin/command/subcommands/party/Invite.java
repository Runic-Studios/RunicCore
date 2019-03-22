package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.command.util.TabCompleteUtil;
import us.fortherealm.plugin.parties.Party;

import java.util.List;

public class Invite implements SubCommand {

	private PartySC party;
	private Plugin plugin = FTRCore.getInstance();

	public Invite(PartySC party) {
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

        // if the sender does not specify a player
        if (args.length == 1) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You must specify a player with "
                            + ChatColor.YELLOW + "/party invite <player>");
            return;
        }

        if (party == null) {
            Bukkit.dispatchCommand(sender, "party create");
            party = FTRCore.getPartyManager().getPlayerParty(sender);
        }

        if ((args[1]).equals("nearby")) {

            for (Entity entity : sender.getNearbyEntities(10, 10, 10)) {

                if (!(entity instanceof Player)) { return; }

                // only the leader can invite others
                if (!(party.getLeader().equals(sender.getUniqueId()))) {
                    sender.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.RED + "You are not the party leader!");
                    return;
                }

                // grab the sender's stored name
                Object storedNameSender = plugin.getConfig().get(sender.getUniqueId() + ".info.name");
                String senderNameToString = storedNameSender.toString();

                // grab the player's stored name
                Object storedNameTarget = plugin.getConfig().get(entity.getUniqueId() + ".info.name");
                String targetNameToString = storedNameTarget.toString();

                // if the specified player is already in the party
                if (party.hasMember(entity.getUniqueId())) {
                    sender.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.WHITE + targetNameToString
                                    + ChatColor.RED + " is already in the party!");
                    return;
                }

                // if the sender specifies a player correctly
                if (FTRCore.getPartyManager().addInvite(new us.fortherealm.plugin.parties.Invite(party, (Player) entity))) {

                    party.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.WHITE + senderNameToString
                                    + ChatColor.GRAY + " invited "
                                    + ChatColor.WHITE + targetNameToString
                                    + ChatColor.GRAY + " to the party!");

                    entity.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.GRAY + "You have been invited to "
                                    + ChatColor.WHITE + senderNameToString
                                    + ChatColor.GRAY + "'s party! Type "
                                    + ChatColor.YELLOW + "/party join "
                                    + ChatColor.GRAY + "to accept!");
                }
            }
        } else {

            Player target = Bukkit.getPlayer(args[1]);

            // if the specified invitee cannot be found
            if (target == null) {
                sender.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.RED + "Player "
                                + ChatColor.WHITE + args[1]
                                + ChatColor.RED + " not found.");
                return;
            }

//            if (party == null) {
//                Bukkit.dispatchCommand(sender, "party create");
//                party = FTRCore.getPartyManager().getPlayerParty(sender);
//            }

            // only the leader can invite others
            if (!(party.getLeader().equals(sender.getUniqueId()))) {
                sender.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.RED + "You are not the party leader!");
                return;
            }

            // grab the sender's stored name
            Object storedNameSender = plugin.getConfig().get(sender.getUniqueId() + ".info.name");
            String senderNameToString = storedNameSender.toString();

            // grab the player's stored name
            Object storedNameTarget = plugin.getConfig().get(target.getUniqueId() + ".info.name");
            String targetNameToString = storedNameTarget.toString();

            // if the specified player is already in the party
            if (party.hasMember(target.getUniqueId())) {
                sender.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + targetNameToString
                                + ChatColor.RED + " is already in the party!");
                return;
            }

            // if the sender specifies a player correctly
            if (FTRCore.getPartyManager().addInvite(new us.fortherealm.plugin.parties.Invite(party, target))) {

                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + senderNameToString
                                + ChatColor.GRAY + " invited "
                                + ChatColor.WHITE + targetNameToString
                                + ChatColor.GRAY + " to the party!");

                target.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.GRAY + "You have been invited to "
                                + ChatColor.WHITE + senderNameToString
                                + ChatColor.GRAY + "'s party! Type "
                                + ChatColor.YELLOW + "/party join "
                                + ChatColor.GRAY + "to accept!");
            }
        }
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
