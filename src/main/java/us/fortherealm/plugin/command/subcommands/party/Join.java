package us.fortherealm.plugin.command.subcommands.party;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.command.subcommands.SubCommand;
import us.fortherealm.plugin.command.supercommands.PartySC;
import us.fortherealm.plugin.parties.Invite;
import us.fortherealm.plugin.parties.Party;
import us.fortherealm.plugin.nametags.NameTagChanger;

import java.util.List;

public class Join implements SubCommand {

    private PartySC party;
    private NameTagChanger nameTagChanger = new NameTagChanger();
    private Plugin plugin = FTRCore.getInstance();

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

        Party party = FTRCore.getPartyManager().getPlayerParty(sender);

        // sender can't double-join parties
        if (party != null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You are already in a party!");
            return;
        }

        Invite invite = FTRCore.getPartyManager().getActiveInvite(sender);

        // if the player has no active invite
        if (invite == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "You don't have any invites!");
            return;
        }

        Party partyLead = FTRCore.getPartyManager().getPlayerParty(invite.getInviter());

        // if the party is disbanded before the player joins, remove the invite
        if (partyLead == null) {
            sender.sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.RED + "This party no longer exists!");
            FTRCore.getPartyManager().removeInvite(invite);
            return;
        }

        // grab the inviter's stored name
        Object storedNameInviter = plugin.getConfig().get(invite.getInviter().getUniqueId() + ".info.name");
        String inviterNameToString = storedNameInviter.toString();

        // add the player by uuid to the party
        invite.getParty().addMember(sender.getUniqueId());

        // get this value updated for later
        party = FTRCore.getPartyManager().getPlayerParty(sender);

        // remove the invite from memory
        FTRCore.getPartyManager().removeInvite(invite);

        // inform the player which party they joined
        sender.sendMessage
                (ChatColor.DARK_GREEN + "Party "
                        + ChatColor.GOLD + "» "
                        + ChatColor.GREEN + "You joined "
                        + ChatColor.WHITE + inviterNameToString
                        + ChatColor.GREEN + "'s party!");

        // grab the player's stored name, convert it to a string
        String storedName = plugin.getConfig().get(sender.getUniqueId() + ".info.name").toString();

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

        // update the tablist
        for (Player member : party.getPlayerMembers()) {
            FTRCore.getTabListManager().setupTab(member);
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
