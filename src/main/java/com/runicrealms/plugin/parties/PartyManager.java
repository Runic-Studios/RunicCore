package com.runicrealms.plugin.parties;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartyManager implements Listener {

    private Set<Party> parties;
    private Map<Player, Party> playerParties;

    public PartyManager() {
        this.parties = new HashSet<Party>();
        this.playerParties = new HashMap<Player, Party>();
    }

    public Set<Party> getParties() {
        return this.parties;
    }

    public void updatePlayerParty(Player player, Party party) {
        if (party == null) {
            this.playerParties.remove(player);
        } else {
            this.playerParties.put(player, party);
        }
    }

    public Party getPlayerParty(Player player) {
        if (this.playerParties.containsKey(player)) {
            return this.playerParties.get(player);
        }
        return null;
    }

    public boolean memberHasInvite(Player player) {
        for (Party party : this.parties) {
            if (party.getInvite(player) != null) {
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.playerParties.containsKey(event.getPlayer())) {
            Party party = this.playerParties.get(event.getPlayer());
            if (party.getLeader() == event.getPlayer()) {
                party.sendMessageInChannel("This party has been disbanded &7Reason: leader disconnected");
                for (Player member : party.getMembers()) {
                    RunicCore.getPartyManager().updatePlayerParty(member, null);
                    RunicCore.getTabListManager().setupTab(member);
                }
                RunicCore.getPartyManager().updatePlayerParty(party.getLeader(), null);
                RunicCore.getPartyManager().getParties().remove(party);
            } else {
                party.getMembers().remove(event.getPlayer());
                party.sendMessageInChannel(event.getPlayer().getName() + " has been removed from the party &7Reason: disconnected");
                for (Player member : party.getMembersWithLeader()) {
                    RunicCore.getTabListManager().setupTab(member);
                }
            }
            this.playerParties.remove(event.getPlayer());
        }
        for (Party party : this.parties) {
            Party.Invite invite = party.getInvite(event.getPlayer());
            if (invite != null) {
                invite.inviteAccepted();
                party.getInvites().remove(invite);
            }
        }
    }

}
