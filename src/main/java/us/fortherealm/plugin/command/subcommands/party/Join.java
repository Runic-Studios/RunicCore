package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.kacperduras.protocoltab.ProtocolTabAPI;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Invite;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.nametags.NameTagChanger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Join implements SubCommand {

    private PartySC party;
    private NameTagChanger nameTagChanger = new NameTagChanger();
    private Plugin plugin = Main.getInstance();

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

        // sender can't double-join parties
        if (party != null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You are already in a party!");
            return;
        }

        Invite invite = Main.getPartyManager().getActiveInvite(sender);

        // if the player has no active invite
        if (invite == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You don't have any invites!");
            return;
        }

        Party partyLead = Main.getPartyManager().getPlayerParty(invite.getInviter());

        // if the party is disbanded before the player joins, remove the invite
        if (partyLead == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "This party no longer exists!");
            Main.getPartyManager().removeInvite(invite);
            return;
        }

        // grab the inviter's stored name
        Object storedNameInviter = plugin.getConfig().get(invite.getInviter().getUniqueId() + ".info.name");
        String inviterNameToString = storedNameInviter.toString();

        // add the player by uuid to the party
        invite.getParty().addMember(sender.getUniqueId());

        // get this value updated for later
        party = Main.getPartyManager().getPlayerParty(sender);

        // remove the invite from memory
        Main.getPartyManager().removeInvite(invite);

        // inform the player which party they joined
        sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.GREEN + "You joined "
                        + ChatColor.WHITE + inviterNameToString
                        + ChatColor.GREEN + "'s party!");

        // grab the player's stored name
        // convert it to a string
        String storedName = plugin.getConfig().get(sender.getUniqueId() + ".info.name").toString();
        String storedLeaderName = plugin.getConfig().get(party.getLeader() + ".info.name").toString();

        // inform the party members of a new member, disclude the sender
        invite.getParty().sendOtherMembersMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.WHITE + storedName
                        + ChatColor.GREEN + " joined the party!", sender.getUniqueId());

        // update the joiner's name for current members
        nameTagChanger.changeNameParty(party, sender, ChatColor.GREEN + storedName);

        // update the party members' name colors for the joiner
        nameTagChanger.showPartyNames(party, sender);

        // update partyCount, player list names
        int partyCount = party.getPartySize();
        for (Player member : party.getPlayerMembers()) {
            ProtocolTabAPI.getTablist(member).setSlot(40, "  &a&n Party (" + partyCount + ") &r");

            for (int k = 0; k < party.getPartyNames().size() && k < 20; k++) {

                if (party.getPartyNames().get(k) == null) { continue; }

                if (party.getPartyNames().get(k).equals(storedLeaderName)) {
                    ProtocolTabAPI.getTablist(member).setSlot(k + 41, ChatColor.GREEN + "★ " + ChatColor.WHITE + party.getPartyNames().get(k));
                } else {
                    ProtocolTabAPI.getTablist(member).setSlot(k + 41, party.getPartyNames().get(k));
                }
            }
            ProtocolTabAPI.getTablist(member).update();
        }
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
