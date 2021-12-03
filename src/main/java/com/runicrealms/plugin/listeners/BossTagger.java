package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossTagger implements Listener {

    private static final double DAMAGE_PERCENT = .05; // threshold to receive loot

    private final HashMap<UUID, HashMap<UUID, Integer>> bossFighters; // a single boss is mapped to many players (damage threshold tracked here)
    private final HashMap<UUID, HashSet<UUID>> bossLooters;

    public BossTagger() {
        // todo: running task to clear boss looters
        bossFighters = new HashMap<>();
        bossLooters = new HashMap<>();
    }

    /**
     * Prepare boss on spawn
     */
    @EventHandler
    public void onBossSpawn(MythicMobSpawnEvent e) {
        MythicMob boss = e.getMobType();
        if (!boss.hasFaction()) return;
        if (!boss.getFaction().equalsIgnoreCase("boss")) return;
        HashMap<UUID, Integer> fighters = new HashMap<>();
        HashSet<UUID> looters = new HashSet<>();
        bossFighters.put(e.getEntity().getUniqueId(), fighters);
        bossLooters.put(e.getEntity().getUniqueId(), looters);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {
        trackBossDamage(e.getPlayer(), e.getVictim(), e.getAmount());
    }

    @EventHandler
    public void onSpellDamage(SpellDamageEvent e) {
        trackBossDamage(e.getPlayer(), e.getVictim(), e.getAmount());
    }

    @EventHandler
    public void onBossDeath(MythicMobDeathEvent e) {
        if (!isBoss(e.getEntity().getUniqueId())) return;
        if (!bossFighters.containsKey(e.getMob().getUniqueId())) return;
        bossFighters.get(e.getEntity().getUniqueId()).clear(); // clear damage tracking map
        // todo: send some information here?
    }

    /**
     * Keeps track of damage during boss fight to determine who gets loot priority.
     *
     * @param player      who damaged boss
     * @param entity      the boss
     * @param eventAmount the damage from the event
     */
    private void trackBossDamage(Player player, Entity entity, int eventAmount) {
        if (!isBoss(entity.getUniqueId())) return;
        if (bossLooters.get(entity.getUniqueId()) == null) return;
        if (bossLooters.get(entity.getUniqueId()).contains(player.getUniqueId())) return;
        UUID playerId = player.getUniqueId();
        UUID bossId = entity.getUniqueId();
        int maxHP = (int) ((ActiveMob) entity).getEntity().getMaxHealth();
        double threshold = maxHP * DAMAGE_PERCENT;
        if (!bossFighters.get(bossId).containsKey(playerId))
            bossFighters.get(bossId).put(playerId, 0);
        int currentDamageToBossFromPlayer = bossFighters.get(bossId).get(playerId);
        bossFighters.get(bossId).put(playerId, currentDamageToBossFromPlayer + eventAmount);
        currentDamageToBossFromPlayer = bossFighters.get(entity.getUniqueId()).get(playerId);
        if (currentDamageToBossFromPlayer >= threshold)
            bossLooters.get(entity.getUniqueId()).add(playerId);
    }

    // todo: belongs in API for RunicItems

    /**
     * This method drops an item in the world with priority.
     *
     * @param bossId    the uuid of boss entity
     * @param location  location to drop the loot
     * @param itemStack to be dropped
     */
    public void dropTaggedBossLoot(UUID bossId, Location location, ItemStack itemStack) {
        ConcurrentHashMap<ItemStack, HashSet<UUID>> priorityItems = RunicCore.getMobTagger().getPriorityItems();
        HashSet<UUID> temp = new HashSet<>();
        priorityItems.put(itemStack, temp);
        ConcurrentHashMap<ItemStack, Long> priorityTimers = RunicCore.getMobTagger().getPriorityTimers();
        for (UUID id : bossLooters.get(bossId)) {
            priorityItems.get(itemStack).add(id);
        }
        priorityTimers.put(itemStack, System.currentTimeMillis());
        Objects.requireNonNull(location.getWorld()).dropItem(location, itemStack);
        bossLooters.get(bossId).clear(); // clear looters list
    }

    // todo: so here's what we need to do. we need to make this an api method. then test item pickup event, make sure this works

    /**
     * Get a set of players who should receive priority boss loot (and tokens)
     *
     * @param bossId uuid of the boss
     * @return a set of uuid's of players
     */
    public HashSet<UUID> getBossLooters(UUID bossId) {
        return bossLooters.get(bossId);
    }

    /**
     * Checks whether the given mob is a boss (based on faction)
     *
     * @param entityId uuid of the entity
     * @return true if mob is a boss
     */
    public static boolean isBoss(UUID entityId) {
        if (!MythicMobs.inst().getMobManager().getActiveMob(entityId).isPresent()) return false;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(entityId).get();
        if (am.getFaction() == null) return false;
        return (am.getFaction().equalsIgnoreCase("boss"));
    }
}
