package com.runicrealms.plugin.party.event;

import com.runicrealms.plugin.party.Party;
import org.bukkit.entity.Player;

public class PartyJoinEvent extends PartyEvent {

    private final Player member;

    public PartyJoinEvent(Party party, Player member) {
        super(party);
        this.member = member;
    }

    public Player getMember() {
        return member;
    }
}
