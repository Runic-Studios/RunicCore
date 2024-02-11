package com.runicrealms.plugin.party.event;

import com.runicrealms.plugin.party.Party;

public class PartyCreateEvent extends PartyEvent { // Called before any players are joined

    public PartyCreateEvent(Party party) {
        super(party);
    }

}
