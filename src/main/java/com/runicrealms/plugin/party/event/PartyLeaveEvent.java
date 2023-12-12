package com.runicrealms.plugin.party.event;

import com.runicrealms.plugin.party.Party;
import org.bukkit.entity.Player;

/**
 * Called before the member is removed from the party
 */
public class PartyLeaveEvent extends PartyEvent {

    private final Player leaver;
    private final LeaveReason leaveReason;

    public PartyLeaveEvent(Party party, Player leaver, LeaveReason leaveReason) {
        super(party);
        this.leaver = leaver;
        this.leaveReason = leaveReason;
    }

    public Player getLeaver() {
        return leaver;
    }

    public LeaveReason getLeaveReason() {
        return leaveReason;
    }
}
