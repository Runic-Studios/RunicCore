package us.fortherealm.plugin.parties;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import us.fortherealm.plugin.FTRCore;
import us.fortherealm.plugin.nametags.NameTagChanger;

public class PartyDisconnect implements Listener {

    private Plugin plugin = FTRCore.getInstance();

    @EventHandler
    public void onMemberDisconnect(PlayerQuitEvent e) {

        Player player = e.getPlayer();
        Party party = FTRCore.getPartyManager().getPlayerParty(player);
        String storedName = plugin.getConfig().get(player.getUniqueId() + ".info.name").toString();

        if (party == null) {

            return;

        } else {

            // remove player from party
            party.removeMember(player.getUniqueId());

            // if the new member count is less than 1, just disband the party
            if (party.getPartySize() < 1) {
                FTRCore.getPartyManager().disbandParty(party);
            }  else {

                // if the player who disconnected was NOT the party lead
                if (!(party.getLeader().equals(player.getUniqueId()))) {
                    party.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.WHITE + storedName
                                    + ChatColor.RED + " left the party by disconnecting.");

                    // update the player list for other party members
                    for (Player member : party.getPlayerMembers()) {
                        FTRCore.getTabListManager().setupTab(member);
                    }
                    return;
                }

                // party leader is set to whoever is now in position [0]
                party.setLeader(party.getMemberUUID(0));
                String storedNameLead = plugin.getConfig().get(party.getLeader() + ".info.name").toString();

                // grab the new Player newLeader from their uuid in the party array
                //Player newLeader = Bukkit.getPlayer(UUID.fromString(party.getLeader().toString()));

                // inform members of leader change
                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + storedName
                                + ChatColor.RED + " left the party by disconnecting. "
                                + ChatColor.WHITE + storedNameLead
                                + ChatColor.GREEN + " is now the party leader!");

                // update the tablist
                for (Player member : party.getPlayerMembers()) {
                    FTRCore.getTabListManager().setupTab(member);
                }
            }
        }
    }

    public static void updatePartyNames(Party party, Player target, Plugin plugin, NameTagChanger nameTagChanger) {
        for (Player member : party.getPlayerMembers()) {
            if (member == target) {
                continue;
            }
            String memberName = plugin.getConfig().get(member.getUniqueId() + ".info.name").toString();
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (plugin.getConfig().getBoolean(member.getUniqueId() + ".outlaw.enabled", true)) {
                        nameTagChanger.changeNameGlobal(member, ChatColor.RED + memberName);
                    } else {
                        nameTagChanger.changeNameGlobal(member, ChatColor.WHITE + memberName);
                    }
                }
            }.runTaskLater(plugin, 10);
        }
    }
}
