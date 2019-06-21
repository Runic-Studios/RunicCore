package com.runicrealms.plugin.command.subcommands.party;

import com.runicrealms.plugin.command.supercommands.PartySC;
import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.parties.Party;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.command.subcommands.SubCommand;
import com.runicrealms.plugin.command.util.TabCompleteUtil;

import java.util.List;

public class Invite implements SubCommand {

	private PartySC party;
	private Plugin plugin = RunicCore.getInstance();

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

        Party party = RunicCore.getPartyManager().getPlayerParty(sender);

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
            party = RunicCore.getPartyManager().getPlayerParty(sender);
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

                // if the specified player is already in the party
                if (party.hasMember(entity.getUniqueId())) {
                    sender.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.WHITE + entity.getName()
                                    + ChatColor.RED + " is already in the party!");
                    return;
                }

                // if the sender specifies a player correctly
                if (RunicCore.getPartyManager().addInvite(new com.runicrealms.plugin.parties.Invite(party, (Player) entity))) {

                    party.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.WHITE + sender.getName()
                                    + ChatColor.GRAY + " invited "
                                    + ChatColor.WHITE + entity.getName()
                                    + ChatColor.GRAY + " to the party!");

                    entity.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.GRAY + "You have been invited to "
                                    + ChatColor.WHITE + sender.getName()
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

            // only the leader can invite others
            if (!(party.getLeader().equals(sender.getUniqueId()))) {
                sender.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.RED + "You are not the party leader!");
                return;
            }

            // if the specified player is already in the party
            if (party.hasMember(target.getUniqueId())) {
                sender.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + target.getName()
                                + ChatColor.RED + " is already in the party!");
                return;
            }

            // if the specified player is more than 15 levels away from the inviter
            if (Math.abs(sender.getLevel()-target.getLevel()) > 15) {
                sender.sendMessage(ChatColor.RED + "Specified player is outside party level range [15].");
                return;
            }

            // if the inviter is an outlaw and the invitee is NOT an outlaw
            if (OutlawManager.isOutlaw(sender) && !OutlawManager.isOutlaw(target)) {
                sender.sendMessage(ChatColor.RED + "Outlaws may only party with other outlaws.");
                return;
            }

            // if the inviter is NOT an outlaw and the invitee is an outlaw
            if (!OutlawManager.isOutlaw(sender) && OutlawManager.isOutlaw(target)) {
                sender.sendMessage(ChatColor.RED + "Specified player is an outlaw. Outlaws may only party with other outlaws.");
                return;
            }

            // if the sender specifies a player correctly
            if (RunicCore.getPartyManager().addInvite(new com.runicrealms.plugin.parties.Invite(party, target))) {

                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + sender.getName()
                                + ChatColor.GRAY + " invited "
                                + ChatColor.WHITE + target.getName()
                                + ChatColor.GRAY + " to the party!");

                target.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.GRAY + "You have been invited to "
                                + ChatColor.WHITE + sender.getName()
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
