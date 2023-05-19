package com.runicrealms.plugin.party;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.PartyAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PartyManager implements Listener, PartyAPI {

    private final Set<Party> parties;
    private final Map<UUID, Party> playerParties;

    public PartyManager() {
        this.parties = new HashSet<>();
        this.playerParties = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public boolean canJoinParty(UUID uuid) {
        return this.getParty(uuid) == null;
    }

    @Override
    public Set<Party> getParties() {
        return this.parties;
    }

    public Party getParty(UUID uuid) {
        if (this.playerParties.containsKey(uuid)) {
            return this.playerParties.get(uuid);
        }
        return null;
    }

    @Override
    public boolean hasParty(UUID uuid) {
        return this.playerParties.containsKey(uuid);
    }

    @Override
    public boolean isPartyMember(UUID first, Player second) {
        if (!RunicCore.getPartyAPI().hasParty(first)) return false;
        if (!RunicCore.getPartyAPI().hasParty(second.getUniqueId())) return false;
        return RunicCore.getPartyAPI().getParty(first).hasMember(second);
    }

    @Override
    public boolean memberHasInvite(Player player) {
        for (Party party : this.parties) {
            if (party.getInvite(player) != null) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updatePlayerParty(UUID uuid, Party party) {
        if (party == null) {
            this.playerParties.remove(uuid);
        } else {
            this.playerParties.put(uuid, party);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.playerParties.containsKey(event.getPlayer().getUniqueId())) {
            Party party = this.playerParties.get(event.getPlayer().getUniqueId());
            if (party.getLeader() == event.getPlayer()) {
                party.sendMessageInChannel("This party has been disbanded &7Reason: leader disconnected");
                for (Player member : party.getMembers()) {
                    updatePlayerParty(member.getUniqueId(), null);
                    RunicCore.getTabAPI().setupTab(member);
                }
                RunicCore.getPartyAPI().updatePlayerParty(party.getLeader().getUniqueId(), null);
                RunicCore.getPartyAPI().getParties().remove(party);
            } else {
                party.getMembers().remove(event.getPlayer());
                party.sendMessageInChannel(event.getPlayer().getName() + " has been removed from the party &7Reason: disconnected");
                for (Player member : party.getMembersWithLeader()) {
                    RunicCore.getTabAPI().setupTab(member);
                }
            }
            this.playerParties.remove(event.getPlayer().getUniqueId());
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
