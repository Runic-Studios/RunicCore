package com.runicrealms.plugin.item;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.HologramUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class MobTagger implements Listener {

    private static final int PRIO_TIME = 10;
    private static final int TAG_TIME = 10;

    private HashMap<UUID, UUID> taggedMobs;
    private HashMap<UUID, Long> taggedTimers;
    private HashMap<ItemStack, UUID> prioItems;
    private HashMap<ItemStack, Long> prioTimers;

    public MobTagger() {
        taggedMobs = new HashMap<>();
        taggedTimers = new HashMap<>();
        prioItems = new HashMap<>();
        prioTimers = new HashMap<>();
        new BukkitRunnable() {
            @Override
            public void run() {
                removeTags();
                removePriority();
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0L, 20L);
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
                Bukkit.broadcastMessage("player no longer has a tag");
                taggedMobs.remove(plID);
                Bukkit.broadcastMessage("mob associated with " + Bukkit.getPlayer(plID).getName() + " is no longer tagged");
            }
        }
    }

    private void removePriority() {
        for (ItemStack itemStack : prioTimers.keySet()) {
            long startTime = prioTimers.get(itemStack);
            if (System.currentTimeMillis() - startTime >= PRIO_TIME*1000) {
                Bukkit.broadcastMessage("item no longer has priority");
                prioTimers.remove(itemStack);
                prioItems.remove(itemStack);
            }
        }
    }

    /**
     * This method drops an item in the world with prio
     */
    public void dropTaggedLoot(Player pl, Location loc, ItemStack itemStack) {
        prioItems.put(itemStack, pl.getUniqueId());
        prioTimers.put(itemStack, System.currentTimeMillis());
        pl.getWorld().dropItem(loc, itemStack);
    }

    /**
     * Checks whether a mob is tagged.
     */
    public boolean getIsTagged(UUID mobID) {
        return taggedMobs.values().contains(mobID);
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
     * Prevent players from picking up priority items.
     */
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent e) {
        ItemStack itemStack = e.getItem().getItemStack();
        if (!prioItems.keySet().contains(itemStack)) return;
        if (e.getEntity().getUniqueId().equals(prioItems.get(itemStack))) return;
        e.setCancelled(true);
    }

    /**
     * Tag mob
     */
    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {

        // todo: ignore players, ignore "Boss" mobs
        Player pl = e.getPlayer();
        Entity victim = e.getEntity();
        UUID plID = pl.getUniqueId();
        UUID victimID = victim.getUniqueId();

        if (taggedMobs.keySet().contains(plID)) { // if player is on the list (has tagged ANY mob)
            Bukkit.broadcastMessage("this player has a current tag");
            if (taggedMobs.get(plID).equals(victimID)) { // if mob is tied to player
                Bukkit.broadcastMessage("this mob was already tagged by this player, updating tag");
                taggedTimers.put(plID, System.currentTimeMillis()); // update timer
            }

        } else {
            if (!taggedMobs.keySet().contains(plID) && !getIsTagged(victimID)) { // player is not on list AND mob is not on list
                Bukkit.broadcastMessage("this mob has been tagged for first time");
                HologramUtil.createStaticHologram(pl, pl.getLocation(), ColorUtil.format("&7Tagged by " + pl.getName()), 0, 2.25, 0);
                taggedMobs.put(plID, victimID);
                taggedTimers.put(plID, System.currentTimeMillis());
            }
        }
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        Player pl = e.getPlayer();
        Entity victim = e.getEntity();
    }
}
