package com.runicrealms.plugin.party;

import com.runicrealms.plugin.events.RunicCombatExpEvent;
import com.runicrealms.plugin.events.RunicMobCombatExpEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class PartyExpPayload {
    private static final int MAX_BLOCK_RANGE = 100;
    private final Player player;
    private final Party party;
    private final int rawTotalExp;
    private final int mobLevel;
    private final @Nullable Location hologramLocation;

    /**
     * Method to determine exp distribution for a given party
     *
     * @param player           who triggered event
     * @param party            of player receiving exp
     * @param rawTotalExp      Exp received from the mob kill
     * @param mobLevel         level of the mob killed
     * @param hologramLocation location of hologram to display
     */
    public PartyExpPayload(Player player, Party party, int rawTotalExp, int mobLevel, @Nullable Location hologramLocation) {
        this.player = player;
        this.party = party;
        this.rawTotalExp = rawTotalExp;
        this.mobLevel = mobLevel;
        this.hologramLocation = hologramLocation;
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
        Set<Player> players = party.getMembersWithLeader().stream().filter(this::isValidMember).collect(Collectors.toSet());
        int expForEach = rawTotalExp / players.size();
        for (Player player : players) {
            RunicCombatExpEvent event = new RunicMobCombatExpEvent(expForEach, true, player, this.mobLevel, this.hologramLocation);
            Bukkit.getPluginManager().callEvent(event);
        }

//        Location location = event.getLocation();
//        assert location != null;
//        int mobLevel = event.getMobLevel();
//        int extraAmt = (int) (event.getOriginalAmount() * RunicExpListener.PARTY_BONUS);
//        int nearbyMembers = determineNearbyMembers();
//        int leftOverExp = event.getFinalAmount() - event.getOriginalAmount() - extraAmt;
//
//        for (Player member : party.getMembersWithLeader()) {
//            if (!isValidMember(member)) continue;
//            int memberLv = member.getLevel();
//            if (mobLevel > (memberLv + RunicExpListener.LEVEL_CUTOFF) || mobLevel < (memberLv - RunicExpListener.LEVEL_CUTOFF)) {
//                PlayerLevelUtil.giveExperience(member, 0);
//                RunicExpListener.createExpHologram(member, location, Collections.singletonList(ColorUtil.format("&7+ &c0 &7exp")), 2.5f);
//            } else {
//                String eventExp = ColorUtil.format("&7+ &f" + event.getOriginalAmount() + " &7exp");
//                String partyExp = ColorUtil.format("&a+ " + extraAmt + " &f" + player.getName() + "&7's Party exp");
//                String otherExp = ColorUtil.format("&7+" + leftOverExp + " &8other &7exp");
//                RunicExpListener.createExpHologram
//                        (
//                                member,
//                                location,
//                                Arrays.asList(eventExp, partyExp, otherExp),
//                                2.5f
//                        );
//                RunicCombatExpEvent nestedEvent = new RunicCombatExpEvent()
//                        (
//                                event.getOriginalAmount(),
//                                ((event.getFinalAmount()) / nearbyMembers),
//                                member,
//                                RunicCombatExpEvent.RunicExpSource.PARTY,
//                                mobLevel,
//                                location
//                        );
//                Bukkit.getPluginManager().callEvent(nestedEvent);
//            }
//        }
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
