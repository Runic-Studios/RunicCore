package com.runicrealms.plugin.parties;

import com.runicrealms.plugin.outlaw.OutlawManager;
import com.runicrealms.plugin.scoreboard.ScoreboardHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.runicrealms.plugin.RunicCore;

import java.util.Collections;

public class PartyDisconnect implements Listener {

    @EventHandler
    public void onMemberDisconnect(PlayerQuitEvent e) {

        Player player = e.getPlayer();
        Party party = RunicCore.getPartyManager().getPlayerParty(player);

        if (party == null) {

            return;

        } else {

            // remove player from party
            party.removeMember(player.getUniqueId());

            // if the new member count is less than 1, just disband the party
            if (party.getPartySize() < 1) {
                RunicCore.getPartyManager().disbandParty(party);
            }  else {

                // if the player who disconnected was NOT the party lead
                if (!(party.getLeader().equals(player.getUniqueId()))) {
                    party.sendMessage
                            (ChatColor.DARK_GREEN + "Party "
                                    + ChatColor.GOLD + "» "
                                    + ChatColor.WHITE + player.getName()
                                    + ChatColor.RED + " left the party by disconnecting.");

                    // update the player list for other party members
                    for (Player member : party.getPlayerMembers()) {
                        RunicCore.getTabListManager().setupTab(member);
                    }
                    return;
                }

                // party leader is set to whoever is now in position [0]
                party.setLeader(party.getMemberUUID(0));

                // grab the new Player newLeader from their uuid in the party array
                //Player newLeader = Bukkit.getPlayer(UUID.fromString(party.getLeader().toString()));

                // inform members of leader change
                party.sendMessage
                        (ChatColor.DARK_GREEN + "Party "
                                + ChatColor.GOLD + "» "
                                + ChatColor.WHITE + player.getName()
                                + ChatColor.RED + " left the party by disconnecting. "
                                + ChatColor.WHITE + Bukkit.getPlayer(party.getLeader()).getName()
                                + ChatColor.GREEN + " is now the party leader!");

                // update the tablist
                for (Player member : party.getPlayerMembers()) {
                    RunicCore.getTabListManager().setupTab(member);
                }
            }
        }
    }

    public static void updatePartyNames(Party party, Player leaver) {

        // update the party members' name colors for the leaver (sender)
        for (Player member : party.getPlayerMembers()) {
            String team = "white";
            if (OutlawManager.isOutlaw(member)) {
                team = "outlaw";
            }
            try {
                ScoreboardHandler.updateNamesFor
                        (leaver, member.getScoreboard().getTeam(team),
                                Collections.singletonList(member.getName()));
            } catch (Exception e) {
                Bukkit.broadcastMessage("fuck u");
                e.printStackTrace();
            }
        }

        String team = "white";
        if (OutlawManager.isOutlaw(leaver)) {
            team = "outlaw";
        }
        // update the leaver's name for current members
        for (Player member : party.getPlayerMembers()) {
            try {
                ScoreboardHandler.updateNamesFor
                        (member, leaver.getScoreboard().getTeam(team),
                                Collections.singletonList(leaver.getName()));
            } catch (Exception e) {
                Bukkit.broadcastMessage("fuck u");
                e.printStackTrace();
            }
        }
    }
}
