package us.fortherealm.plugin.parties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import pl.kacperduras.protocoltab.ProtocolTabAPI;
import pl.kacperduras.protocoltab.manager.ProtocolTab;
import us.fortherealm.plugin.Main;
import us.fortherealm.plugin.nametags.NameTagChanger;

import java.util.UUID;

public class PartyDisconnect implements Listener {

    private Plugin plugin = Main.getInstance();

    @EventHandler
    public void onMemberDisconnect(PlayerQuitEvent e) {

        Player player = e.getPlayer();
        Party party = Main.getPartyManager().getPlayerParty(player);
        String storedName = plugin.getConfig().get(player.getUniqueId() + ".info.name").toString();

        if (party == null) {

            return;

        } else {

            // remove player from party
            party.removeMember(player.getUniqueId());

            // if the new member count is less than 1, just disband the party
            if (party.getPartySize() < 1) {
                Main.getPartyManager().disbandParty(party);
            }  else {

                // if the player who disconnected was NOT the party lead
                if (!(party.getLeader().equals(player.getUniqueId()))) {
                    party.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.WHITE + storedName
                                    + ChatColor.RED + " left the party by disconnecting.");

                    // update the player list for other party members
                    int partyCount = party.getPartySize();
                    updatePartyList(party, partyCount);
                    return;
                }

                // party leader is set to whoever is now in position [0]
                party.setLeader(party.getMemberUUID(0));
                String storedNameLead = plugin.getConfig().get(party.getLeader() + ".info.name").toString();

                // grab the new Player newLeader from their uuid in the party array
                Player newLeader = Bukkit.getPlayer(UUID.fromString(party.getLeader().toString()));

                // inform members of leader change
                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + storedName
                                + ChatColor.RED + " left the party by disconnecting. "
                                + ChatColor.WHITE + storedNameLead
                                + ChatColor.GREEN + " is now the party leader!");

                // update the player list for other party members
                int partyCount = party.getPartySize();
                updatePartyList(party, partyCount);
            }
        }
    }

    public static void updatePartyList(Party party, int partyCount) {
        for (Player member : party.getPlayerMembers()) {
            ProtocolTabAPI.getTablist(member).setSlot(40, "  &a&n Party (" + partyCount + ") &r");

            // reset the party column
            for (int j = 41; j < 60; j++) {
                ProtocolTabAPI.getTablist(member).setSlot(j, ProtocolTab.BLANK_TEXT);
            }

            for (int k = 0; k < party.getPartyNames().size() && k < 20; k++) {

                if (party.getPartyNames().get(k) == null) {
                    continue;
                }

                if (k == 0) {
                    ProtocolTabAPI.getTablist(member).setSlot(k + 41, ChatColor.GREEN + "★ " + ChatColor.WHITE + party.getPartyNames().get(k));
                } else {
                    ProtocolTabAPI.getTablist(member).setSlot(k + 41, party.getPartyNames().get(k));
                }
            }
            ProtocolTabAPI.getTablist(member).update();
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
