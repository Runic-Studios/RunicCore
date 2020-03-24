package com.runicrealms.plugin.item;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MobTagger implements Listener {

    private static final int PRIO_TIME = 10;
    private static final int TAG_TIME = 10;

    private ConcurrentHashMap<UUID, UUID> taggedMobs;
    private ConcurrentHashMap<UUID, Long> taggedTimers;
    private ConcurrentHashMap<ItemStack, HashSet<UUID>> prioItems;
    private ConcurrentHashMap<ItemStack, Long> prioTimers;

    public MobTagger() {
        taggedMobs = new ConcurrentHashMap<>();
        taggedTimers = new ConcurrentHashMap<>();
        prioItems = new ConcurrentHashMap<>();
        prioTimers = new ConcurrentHashMap<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                removeTags();
                removePriority();
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
    }

    /**
     * This method runs every second, async, and check to see if the current time - long
     * has passed the TAG_TIME to remove mob tags.
     */
    private void removeTags() {
        for (UUID plID : taggedTimers.keySet()) {
            long startTime = taggedTimers.get(plID);
            if (System.currentTimeMillis() - startTime >= TAG_TIME*1000) {
                taggedTimers.remove(plID);
                taggedMobs.remove(plID);
            }
        }
    }

    private void removePriority() {
        for (ItemStack itemStack : prioTimers.keySet()) {
            long startTime = prioTimers.get(itemStack);
            if (System.currentTimeMillis() - startTime >= PRIO_TIME*1000) {
                prioTimers.remove(itemStack);
                prioItems.remove(itemStack);
            }
        }
    }

    /**
     * This method drops an item in the world with prio
     */
    public void dropTaggedLoot(Player pl, Location loc, ItemStack itemStack) {
        HashSet<UUID> temp = new HashSet<>();
        prioItems.put(itemStack, temp);
        prioItems.get(itemStack).add(pl.getUniqueId());
        prioTimers.put(itemStack, System.currentTimeMillis());
        pl.getWorld().dropItem(loc, itemStack);
    }

    /**
     * Checks whether a mob is tagged.
     */
    public boolean getIsTagged(UUID mobID) {
        return taggedMobs.containsValue(mobID);
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

    public ConcurrentHashMap<ItemStack, HashSet<UUID>> getPrioItems() {
        return prioItems;
    }

    public ConcurrentHashMap<ItemStack, Long> getPrioTimers() {
        return prioTimers;
    }

    /**
     * Prevent players from picking up priority items.
     */
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        ItemStack itemStack = e.getItem().getItemStack();
        if (!prioItems.containsKey(itemStack)) return;
        if (prioItems.get(itemStack).contains(e.getEntity().getUniqueId())) return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        tagMob(e.getPlayer(), e.getEntity());
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        tagMob(e.getPlayer(), e.getEntity());
    }

    /**
     * Tag mob
     */
    private void tagMob(Player pl, Entity victim) {

        if (victim instanceof Player) return;

        UUID plID = pl.getUniqueId();
        UUID victimID = victim.getUniqueId();

        if (MythicMobs.inst().getMobManager().getActiveMob(victimID).isPresent()) {
            ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(victimID).get();
            if (am.getFaction() != null && am.getFaction().equalsIgnoreCase("boss")) {
                return;
            }
        }

        if (taggedMobs.containsKey(plID)) { // if player is on the list (has tagged ANY mob)
            if (taggedMobs.get(plID).equals(victimID)) { // if mob is tied to player
                taggedTimers.put(plID, System.currentTimeMillis()); // update timer
            }
        } else {
            if (!taggedMobs.containsKey(plID) && !getIsTagged(victimID)) { // player is not on list AND mob is not on list
                taggedMobs.put(plID, victimID);
                taggedTimers.put(plID, System.currentTimeMillis());
            }
        }
    }
}
