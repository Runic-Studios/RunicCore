package com.runicrealms.plugin.listeners;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellDamageEvent;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import com.runicrealms.plugin.item.lootchests.BossChest;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

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

    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onChestInteract(PlayerInteractEvent e) {
        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!(e.getHand() == EquipmentSlot.HAND)) return;
        if (!e.hasBlock()) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.CHEST) return;
        Block block = e.getClickedBlock();
        Chest chest = (Chest) block.getState();
        BossChest bossChest = BossChest.getFromBlock(RunicCore.getBossTagger().getActiveBossLootChests(), chest);
        if (bossChest == null) return;
        UUID bossId = bossChest.getBossUuid();
        Player player = e.getPlayer();
        if (RunicCore.getBossTagger().getBossLooters(bossId).contains(player.getUniqueId())) {
            bossChest.attemptToOpen(player);
        } else {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Only the slayers of the boss may open the spoils!");
        }
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
     * Maps a boss unique id to a block which represents its chest
     *
     * @return a key, value map of boss uuid to chest
     */
    public HashMap<UUID, BossChest> getActiveBossLootChests() {
        return activeBossLootChests;
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
