package com.runicrealms.plugin.parties;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import com.runicrealms.plugin.RunicCore;

public class Invite {
    private int counter;
    private Party party;
    private Player player;

    public Invite(Party party, Player player)
    {
        this.counter = 60;
        this.party = party;
        this.player = player;
    }

    void update() {
        this.counter -= 1;
        if(this.counter <= 0) {
            RunicCore.getPartyManager().getActiveInvites().remove(this);
        }
    }

    public Party getParty() {
        return this.party;
    }

    public Player getInviter() {
        return Bukkit.getPlayer(getParty().getLeader());
    }
    public Player getInvitedPlayer() {
        return this.player;
    }
}
