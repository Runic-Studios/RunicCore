package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.item.lootchests.BossTagger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobTagger implements Listener {

    private static final int PRIORITY_DURATION = 10; // seconds
    private static final int TAG_TIME = 10; // seconds

    private final ConcurrentHashMap<UUID, UUID> taggedMobs; // maps a single player to a single mob
    private final ConcurrentHashMap<UUID, Long> taggedTimers; // maps players to start time
    private final ConcurrentHashMap<ItemStack, HashSet<UUID>> priorityItems; // maps an itemstack to a list of players
    private final ConcurrentHashMap<ItemStack, Long> priorityTimers; // maps an itemstack to its start time for priority

    public MobTagger() {
        taggedMobs = new ConcurrentHashMap<>();
        taggedTimers = new ConcurrentHashMap<>();
        priorityItems = new ConcurrentHashMap<>();
        priorityTimers = new ConcurrentHashMap<>();
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), this::removeTags, 0, 20L);
    }

    /**
     * This method drops an item in the world with priority to the player who tagged the mob
     *
     * @param player    who tagged the mob
     * @param location  to drop the item
     * @param itemStack to drop
     */
    public void dropTaggedLoot(Player player, Location location, ItemStack itemStack) {
        HashSet<UUID> temp = new HashSet<>();
        priorityItems.put(itemStack, temp);
        priorityItems.get(itemStack).add(player.getUniqueId());
        priorityTimers.put(itemStack, System.currentTimeMillis());
        player.getWorld().dropItem(location, itemStack);
    }

    public ConcurrentHashMap<ItemStack, HashSet<UUID>> getPriorityItems() {
        return priorityItems;
    }

    public ConcurrentHashMap<ItemStack, Long> getPriorityTimers() {
        return priorityTimers;
    }

    /**
     * Returns the tagger of a mob (to determine who to give priority to)
     */
    public Player getTagger(UUID mobID) {
        for (UUID plID : taggedMobs.keySet()) {
            if (taggedMobs.get(plID).equals(mobID)) {
                return Bukkit.getPlayer(plID);
            }
        }
        return null;
    }

    /**
     * Checks whether a mob is tagged.
     */
    public boolean isTagged(UUID mobID) {
        return taggedMobs.containsValue(mobID);
    }

    /**
     * Prevent players from picking up priority items.
     */
    // todo: check this
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onItemPickup(EntityPickupItemEvent e) {
        ItemStack itemStack = e.getItem().getItemStack();
        if (!priorityItems.containsKey(itemStack)) return;
        if (priorityItems.get(itemStack).contains(e.getEntity().getUniqueId())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent e) {
        tagMob(e.getPlayer(), e.getVictim());
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent e) {
        tagMob(e.getPlayer(), e.getVictim());
    }

    /**
     * Removes pickup restrictions on an item
     */
    private void removePriority() {
        for (ItemStack itemStack : priorityTimers.keySet()) {
            long startTime = priorityTimers.get(itemStack);
            if (System.currentTimeMillis() - startTime >= PRIORITY_DURATION * 1000) {
                priorityTimers.remove(itemStack);
                priorityItems.remove(itemStack);
            }
        }
    }

    /**
     * This method runs every second, async, and checks to see if the current time - long
     * has passed the TAG_TIME to remove mob tags. Players get 10 seconds to tag mobs,
     * and if the mob dies, that players has 10 seconds of priority over the loot.
     */
    private void removeTags() {
        for (UUID playerId : taggedTimers.keySet()) {
            long startTime = taggedTimers.get(playerId);
            if (System.currentTimeMillis() - startTime >= TAG_TIME * 1000) {
                taggedTimers.remove(playerId);
                taggedMobs.remove(playerId);
            }
        }
        removePriority();
    }

    /**
     * Attempts to update the tag timer for the given mob. If the mob is linked to another player,
     * this method simply returns. Otherwise, if the player is linked to this mob, it updates the start
     * time value for their tagger.
     *
     * @param player who is tagging the mob
     * @param entity mob to be tagged
     */
    private void tagMob(Player player, Entity entity) {
        if (entity instanceof Player) return;
        UUID playerId = player.getUniqueId();
        UUID entityId = entity.getUniqueId();
        if (BossTagger.isBoss(entityId)) return; // handled separately
        if (taggedMobs.containsKey(playerId)) { // if player is on the list (has tagged ANY mob)
            if (taggedMobs.get(playerId).equals(entityId)) { // if mob is tied to player
                taggedTimers.put(playerId, System.currentTimeMillis()); // update timer ONLY if mob linked to player
            }
        } else {
            if (!taggedMobs.containsKey(playerId) && !isTagged(entityId)) { // player is not on list AND mob is not on list
                taggedMobs.put(playerId, entityId);
                taggedTimers.put(playerId, System.currentTimeMillis());
            }
        }
    }
}
