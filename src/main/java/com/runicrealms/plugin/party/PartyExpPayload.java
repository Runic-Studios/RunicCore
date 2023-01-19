package com.runicrealms.plugin.party;

import com.runicrealms.plugin.events.RunicExpEvent;
import com.runicrealms.plugin.listeners.RunicExpListener;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.Arrays;
import java.util.Collections;

public class PartyExpPayload {
    private static final int MAX_BLOCK_RANGE = 100;
    private final Player player;
    private final Party party;
    private final RunicExpEvent event;
    private final Jedis jedis;

    /**
     * Method to determine exp distribution for a given party
     *
     * @param player who triggered event
     * @param party  of player receiving exp
     * @param event  the original exp event that triggered the payload
     * @param jedis  the jedis resource
     */
    public PartyExpPayload(Player player, Party party, RunicExpEvent event, Jedis jedis) {
        this.player = player;
        this.party = party;
        this.event = event;
        this.jedis = jedis;
    }

    /**
     * Determine how many players to split exp among
     * Only nearby players in the same world should receive their share
     *
     * @return the number of valid nearby party members
     */
    private int determineNearbyMembers() {
        int nearbyMembers = 0;
        for (Player member : party.getMembersWithLeader()) {
            if (!isValidMember(member)) continue;
            nearbyMembers += 1;

        }
        return nearbyMembers;
    }

    public void distributePartyExp() {
        Location location = event.getLocation();
        assert location != null;
        int mobLevel = event.getMobLevel();
        int extraAmt = (int) (event.getOriginalAmount() * RunicExpListener.PARTY_BONUS);
        int nearbyMembers = determineNearbyMembers();
        int leftOverExp = event.getFinalAmount() - event.getOriginalAmount() - extraAmt;

        for (Player member : party.getMembersWithLeader()) {
            if (!isValidMember(member)) continue;
            int memberLv = member.getLevel();
            if (mobLevel > (memberLv + RunicExpListener.LEVEL_CUTOFF) || mobLevel < (memberLv - RunicExpListener.LEVEL_CUTOFF)) {
                PlayerLevelUtil.giveExperience(member, 0, jedis);
                RunicExpListener.createExpHologram(member, location, Collections.singletonList(ColorUtil.format("&7+ &c0 &7exp")), 2.5f);
            } else {
                String eventExp = ColorUtil.format("&7+ &f" + event.getOriginalAmount() + " &7exp");
                String partyExp = ColorUtil.format("&a+ " + extraAmt + " &f" + player.getName() + "&7's Party exp");
                String otherExp = ColorUtil.format("&7+" + leftOverExp + " &8other &7exp");
                RunicExpListener.createExpHologram
                        (
                                member,
                                location,
                                Arrays.asList(eventExp, partyExp, otherExp),
                                2.5f
                        );
                RunicExpEvent nestedEvent = new RunicExpEvent
                        (
                                event.getOriginalAmount(),
                                ((event.getFinalAmount()) / nearbyMembers),
                                member,
                                RunicExpEvent.RunicExpSource.PARTY,
                                mobLevel,
                                location
                        );
                Bukkit.getPluginManager().callEvent(nestedEvent);
            }
        }
    }

    /**
     * Determines if a party member is eligible to receive exp payload
     *
     * @param member to check
     * @return true if member in same world as player and within range
     */
    private boolean isValidMember(Player member) {
        if (player.getLocation().getWorld() != member.getLocation().getWorld()) return false;
        return !(player.getLocation().distanceSquared(member.getLocation()) > MAX_BLOCK_RANGE * MAX_BLOCK_RANGE);
    }
}
