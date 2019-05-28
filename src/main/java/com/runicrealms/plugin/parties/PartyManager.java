package com.runicrealms.plugin.parties;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.RunicCore;

import java.util.ArrayList;

public class PartyManager {
    private ArrayList<Party> activeParties = new ArrayList<>();
    private ArrayList<Invite> activeInvites = new ArrayList<>();

    public PartyManager() {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), () -> {
            for(Party party : activeParties) {
                party.update();
            }
            for(Invite invite : activeInvites) {
                invite.update();
            }
        }, 20L);
    }

    public void addParty(Party party) {
        this.activeParties.add(party);
    }

    public boolean addInvite(Invite invite) {
        Invite pending = null;
        for(Invite activeInvite : activeInvites)
        {
            if(activeInvite.getInvitedPlayer() == invite.getInvitedPlayer()) {
                pending = activeInvite;
            }
        }

        if(pending == null) {
            this.activeInvites.add(invite);
            return true;
        } else {
            invite.getInviter().sendMessage
                    (ChatColor.DARK_GREEN + "Party "
                            + ChatColor.GOLD + "» "
                            + ChatColor.WHITE + invite.getInvitedPlayer().getName()
                            + ChatColor.RED + " already has an invite to another party!");

            return false;
        }
    }

    public void removeParty(Party party) {
        this.activeParties.remove(party);
    }

    public void removeInvite(Invite invite) {
        this.activeInvites.remove(invite);
    }

    public void disbandParty(Party party) {
        party.removeAllMembers();
        this.activeParties.remove(party);
    }

    public Party getPlayerParty(Player player) {
        for(Party party : activeParties) {
            if(party.hasMember(player)) {
                return party;
            }
        }
        return null;
    }

    public Invite getActiveInvite(Player player) {
        for(Invite invite : activeInvites) {
            if(invite.getInvitedPlayer() == player) {
                return invite;
            }
        }

        return null;
    }

    public ArrayList<Party> getActiveParties() {
        return this.activeParties;
    }

    public ArrayList<Invite> getActiveInvites() {
        return this.activeInvites;
    }
}
