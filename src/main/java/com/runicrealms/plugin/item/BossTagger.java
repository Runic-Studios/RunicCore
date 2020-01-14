package com.runicrealms.plugin.item;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.WeaponDamageEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class BossTagger implements Listener {

    private static final int DAMAGE_PERCENT = 10;

    private HashMap<UUID, HashMap<UUID, Integer>> bossFighters;
    private HashMap<UUID, HashSet<UUID>> bossLooters;

    public BossTagger() {
        bossFighters = new HashMap<>();
        bossLooters = new HashMap<>();
    }

    /**
     * Prep boss on spawn
     */
    @EventHandler
    public void onBossSpawn(MythicMobSpawnEvent e) {
        Bukkit.broadcastMessage(ChatColor.GRAY + "mythic mob spawned");
//        if (!MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).isPresent()) return;
//        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).get();
        MythicMob boss = e.getMobType();
        if (!boss.hasFaction()) return;
        if (!boss.getFaction().equalsIgnoreCase("boss")) return;
        HashMap<UUID, Integer> fighters = new HashMap<>();
        HashSet<UUID> looters = new HashSet<>();
        bossFighters.put(e.getEntity().getUniqueId(), fighters);
        bossLooters.put(e.getEntity().getUniqueId(), looters);
        Bukkit.broadcastMessage(ChatColor.GREEN + "Boss tables initialized");
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {

        if (!MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).isPresent()) return;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).get();
        if (am.getFaction() == null) return;
        if (!am.getFaction().equalsIgnoreCase("boss")) return;
        if (bossLooters.get(am.getUniqueId()).contains(e.getPlayer().getUniqueId())) return;

        UUID plID = e.getPlayer().getUniqueId();
        UUID bossID = e.getEntity().getUniqueId();
        int maxHP = (int) am.getEntity().getMaxHealth();
        int threshold = maxHP / DAMAGE_PERCENT;
        if (!bossFighters.get(bossID).containsKey(plID)) bossFighters.get(bossID).put(plID, 0);
        int current = bossFighters.get(bossID).get(plID);
        bossFighters.get(bossID).put(plID, current+e.getAmount());

        Bukkit.broadcastMessage(ChatColor.GREEN + "Player has dealt " + (current+e.getAmount()) + " damage to this boss");
        current = bossFighters.get(e.getEntity().getUniqueId()).get(plID);
        if (current >= threshold) {
            bossLooters.get(e.getEntity().getUniqueId()).add(plID);
            Bukkit.broadcastMessage("player has been added to looters list");
        }
    }

    // todo: add spell damage event

    @EventHandler
    public void onBossDeath(MythicMobDeathEvent e) {

        if (!e.getMob().hasFaction()) return;
        if (!e.getMobType().getFaction().equalsIgnoreCase("boss")) return;
        if (!bossFighters.containsKey(e.getMob().getUniqueId())) return;

        // clear damage tracking
        bossFighters.get(e.getEntity().getUniqueId()).clear();

        // todo: debug and ensure correct looters.
        HashSet<UUID> looters = bossLooters.get(e.getMob().getUniqueId());
        for (UUID id : looters) {
            Bukkit.broadcastMessage(Bukkit.getPlayer(id).getName());
        }

        // delayed task (2) to clear looters, as mob tagger will keep track of prio from here on out.
    }

    /**
     * This method drops an item in the world with prio
     */
    public void dropTaggedBossLoot(Entity boss, Location loc, ItemStack itemStack) {
        HashMap<ItemStack, List<UUID>> prioItems = RunicCore.getMobTagger().getPrioItems();
        HashMap<ItemStack, Long> prioTimers = RunicCore.getMobTagger().getPrioTimers();
        for (UUID id : bossLooters.get(boss.getUniqueId())) {
            prioItems.get(itemStack).add(id);
        }
        prioTimers.put(itemStack, System.currentTimeMillis());
        Objects.requireNonNull(loc.getWorld()).dropItem(loc, itemStack);
    }
}
