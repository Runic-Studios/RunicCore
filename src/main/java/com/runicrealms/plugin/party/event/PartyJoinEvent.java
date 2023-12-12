package com.runicrealms.plugin.party.event;

import com.runicrealms.plugin.party.Party;
import org.bukkit.entity.Player;

/**
 * Called before the member joins the party
 */
public class PartyJoinEvent extends PartyEvent {

    private final Player joining;

    public PartyJoinEvent(Party party, Player joining) {
        super(party);
        this.joining = joining;
    }

    public Player getJoining() {
        return joining;
    }
}
