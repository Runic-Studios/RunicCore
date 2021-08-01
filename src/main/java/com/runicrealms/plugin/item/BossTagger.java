package com.runicrealms.plugin.item;

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
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossTagger implements Listener {

    private static final int DAMAGE_PERCENT = 10;

    private final HashMap<UUID, HashMap<UUID, Integer>> bossFighters;
    private final HashMap<UUID, HashSet<UUID>> bossLooters;

    public BossTagger() {
        bossFighters = new HashMap<>();
        bossLooters = new HashMap<>();
    }

    /**
     * Prep boss on spawn
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

        if (!e.getMob().hasFaction()) return;
        if (!e.getMob().getFaction().equalsIgnoreCase("boss")) return;
        if (!bossFighters.containsKey(e.getMob().getUniqueId())) return;

        // clear damage tracking
        bossFighters.get(e.getEntity().getUniqueId()).clear();

        // delayed task (2) to clear looters, as mob tagger will keep track of prio from here on out.
        new BukkitRunnable() {
            @Override
            public void run() {
                bossLooters.get(e.getEntity().getUniqueId()).clear();
            }
        }.runTaskLater(RunicCore.getInstance(), 40L);
    }

    /**
     * Keeps track of damage during boss fight to determine who gets loot priority.
     */
    private void trackBossDamage(Player pl, Entity en, int eventAmt) {

        if (!MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).isPresent()) return;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(en.getUniqueId()).get();
        if (am.getFaction() == null) return;
        if (!am.getFaction().equalsIgnoreCase("boss")) return;
        if (bossLooters.get(am.getUniqueId()) == null) return;
        if (bossLooters.get(am.getUniqueId()).contains(pl.getUniqueId())) return;

        UUID plID = pl.getUniqueId();
        UUID bossID = en.getUniqueId();
        int maxHP = (int) am.getEntity().getMaxHealth();
        int threshold = maxHP / DAMAGE_PERCENT;
        if (!bossFighters.get(bossID).containsKey(plID)) bossFighters.get(bossID).put(plID, 0);
        int current = bossFighters.get(bossID).get(plID);
        bossFighters.get(bossID).put(plID, current + eventAmt);

        current = bossFighters.get(en.getUniqueId()).get(plID);
        if (current >= threshold) bossLooters.get(en.getUniqueId()).add(plID);
    }

    /**
     * This method drops an item in the world with prio
     */
    public void dropTaggedBossLoot(UUID bossID, Location loc, ItemStack itemStack) {
        ConcurrentHashMap<ItemStack, HashSet<UUID>> prioItems = RunicCore.getMobTagger().getPrioItems();
        HashSet<UUID> temp = new HashSet<>();
        prioItems.put(itemStack, temp);
        ConcurrentHashMap<ItemStack, Long> prioTimers = RunicCore.getMobTagger().getPrioTimers();
        for (UUID id : bossLooters.get(bossID)) {
            prioItems.get(itemStack).add(id);
        }
        prioTimers.put(itemStack, System.currentTimeMillis());
        Objects.requireNonNull(loc.getWorld()).dropItem(loc, itemStack);
    }

    public boolean isBoss(UUID entityID) {
        if (!MythicMobs.inst().getMobManager().getActiveMob(entityID).isPresent()) return false;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(entityID).get();
        if (am.getFaction() == null) return false;
        return (am.getFaction().equalsIgnoreCase("boss"));
    }
}
