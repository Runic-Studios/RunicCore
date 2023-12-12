package com.runicrealms.plugin.party.event;

import com.runicrealms.plugin.party.Party;

public class PartyCreateEvent extends PartyEvent {

    public PartyCreateEvent(Party party) {
        super(party);
    }

}
