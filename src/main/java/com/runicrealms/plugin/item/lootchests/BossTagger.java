package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class BossTagger implements Listener {

    private static final double DAMAGE_PERCENT = .05; // threshold to receive loot

    private final HashMap<UUID, HashMap<UUID, Integer>> bossFighters; // a single boss is mapped to many players (damage threshold tracked here)
    private final HashMap<UUID, HashSet<UUID>> bossLooters;
    private final HashMap<UUID, BossChest> activeBossLootChests;

    public BossTagger() {
        bossFighters = new HashMap<>();
        bossLooters = new HashMap<>();
        activeBossLootChests = new HashMap<>();
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

    /**
     * Maps a boss unique id to a block which represents its chest
     *
     * @return a key, value map of boss uuid to chest
     */
    public HashMap<UUID, BossChest> getActiveBossLootChests() {
        return activeBossLootChests;
    }

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
     * Generate loot for each slayer when the boss is defeated
     */
    @EventHandler
    public void onBossDeath(MythicMobDeathEvent event) {
        if (!isBoss(event.getEntity().getUniqueId())) return;
        if (!bossFighters.containsKey(event.getMob().getUniqueId())) return;
        bossFighters.get(event.getEntity().getUniqueId()).clear(); // clear damage tracking map
    }

    /**
     * Prepare boss on spawn
     */
    @EventHandler
    public void onBossSpawn(MythicMobSpawnEvent event) {
        MythicMob boss = event.getMobType();
        if (!boss.hasFaction()) return;
        if (!boss.getFaction().equalsIgnoreCase("boss")) return;
        HashMap<UUID, Integer> fighters = new HashMap<>();
        HashSet<UUID> looters = new HashSet<>();
        bossFighters.put(event.getEntity().getUniqueId(), fighters);
        bossLooters.put(event.getEntity().getUniqueId(), looters);
    }

    @EventHandler
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        trackBossDamage(event.getPlayer(), event.getVictim(), event.getAmount());
    }

    @EventHandler
    public void onSpellDamage(MagicDamageEvent event) {
        trackBossDamage(event.getPlayer(), event.getVictim(), event.getAmount());
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
        int maxHP = (int) ((LivingEntity) entity).getMaxHealth();
        double threshold = maxHP * DAMAGE_PERCENT;
        if (!bossFighters.get(bossId).containsKey(playerId))
            bossFighters.get(bossId).put(playerId, 0);
        int currentDamageToBossFromPlayer = bossFighters.get(bossId).get(playerId);
        bossFighters.get(bossId).put(playerId, currentDamageToBossFromPlayer + eventAmount);
        currentDamageToBossFromPlayer = bossFighters.get(entity.getUniqueId()).get(playerId);
        if (currentDamageToBossFromPlayer >= threshold)
            bossLooters.get(entity.getUniqueId()).add(playerId);
    }
}
