package com.runicrealms.plugin.events;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.CombatManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a player enters combat with anything other than another player
 */
public class EnterCombatEvent extends Event implements Cancellable {
    private static final int PARTY_TAG_RANGE = 100;
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private CombatManager.CombatType combatType;
    private boolean isCancelled = false;

    /**
     * Create an event for combat for our other plugins to listen to
     *
     * @param player the player who will be tagged in combat
     */
    public EnterCombatEvent(Player player, CombatManager.CombatType combatType) {
        this.player = player;
        this.combatType = combatType;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    /**
     * Both the player AND their party are tagged in combat
     */
    public static void tagPlayerAndPartyInCombat(Player player, CombatManager.CombatType combatType) {
        RunicCore.getCombatAPI().enterCombat(player.getUniqueId(), combatType);
        tagPlayerPartyInCombat(player, combatType);
    }

    /**
     * The entire party gets tagged in combat to avoid all sorts of exploits
     *
     * @param player the player who initiated the combat tag
     */
    private static void tagPlayerPartyInCombat(Player player, CombatManager.CombatType combatType) {
        if (RunicCore.getPartyAPI().getParty(player.getUniqueId()) == null) return;
        for (Player member : RunicCore.getPartyAPI().getParty(player.getUniqueId()).getMembersWithLeader()) {
            if (member == player) continue;
            if (player.getLocation().getWorld() != member.getLocation().getWorld()) continue;
            if (player.getLocation().distanceSquared(member.getLocation()) > PARTY_TAG_RANGE * PARTY_TAG_RANGE)
                continue; // only tag players in 100 block range
            RunicCore.getCombatAPI().enterCombat(member.getUniqueId(), combatType);
        }
    }

    public CombatManager.CombatType getCombatType() {
        return combatType;
    }

    public void setCombatType(CombatManager.CombatType combatType) {
        this.combatType = combatType;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return this.player;
    }

    @Override
    public boolean isCancelled() {
        return this.isCancelled;
    }

    @Override
    public void setCancelled(boolean arg0) {
        this.isCancelled = arg0;
    }
}
