package com.runicrealms.plugin.parties;

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
            this.playerParties.remove(event.getPlayer());
        }
        for (Party party : this.parties) {
            Party.Invite invite = party.getInvite(event.getPlayer());
            if (invite != null) {
                invite.inviteAccepted();
                party.getInvites().remove(invite);
            }
        }
        // TODO - check if player is in party and disband if leader
    }

}
