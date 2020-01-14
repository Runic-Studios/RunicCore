package com.runicrealms.plugin.item;

import com.runicrealms.plugin.events.WeaponDamageEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BossTagger {

    private static final int DAMAGE_PERCENT = 10;

    private HashMap<UUID, List<UUID>> bossFighters;
    private HashMap<UUID, List<UUID>> bossLooters;
    private HashMap<UUID, Integer> damageTracker;

    public BossTagger() {
        bossFighters = new HashMap<>();
        bossLooters = new HashMap<>();
        damageTracker = new HashMap<>();
    }

    /**
     * Prep boss on spawn
     */
    @EventHandler
    public void onBossSpawn(EntitySpawnEvent e) {
        if (!MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).isPresent()) return;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).get();
        if (am.getFaction() == null) return;
        if (!am.getFaction().equalsIgnoreCase("boss")) return;
        List<UUID> fighters = new ArrayList<>();
        List<UUID> looters = new ArrayList<>();
        bossFighters.put(e.getEntity().getUniqueId(), fighters);
        bossLooters.put(e.getEntity().getUniqueId(), looters);
    }

    @EventHandler
    public void onWeaponDamage(WeaponDamageEvent e) {

        if (!MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).isPresent()) return;
        ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(e.getEntity().getUniqueId()).get();
        if (am.getFaction() == null) return;
        if (!am.getFaction().equalsIgnoreCase("boss")) return;

        Player pl = e.getPlayer();
        int maxHP = (int) am.getEntity().getMaxHealth();
        int threshold = maxHP / DAMAGE_PERCENT;
        bossFighters.get(e.getEntity().getUniqueId()).add(e.getPlayer().getUniqueId());
        int current = damageTracker.get(pl.getUniqueId());
        damageTracker.put(pl.getUniqueId(), current+e.getAmount());

        // add to looters list
        if (damageTracker.get(pl.getUniqueId()) > threshold) bossLooters.get(e.getEntity().getUniqueId()).add(pl.getUniqueId());
    }

    @EventHandler
    public void onBossDeath(MythicMobDeathEvent e) {

        if (!e.getMob().hasFaction()) return;
        if (!e.getMobType().getFaction().equalsIgnoreCase("boss")) return;
        if (!bossFighters.containsKey(e.getMob().getUniqueId())) return;

        // clear damage tracking
        for (UUID plID : bossFighters.get(e.getMob().getUniqueId())) {
            damageTracker.remove(plID);
        }

        // spawn chest

        // todo: debug
        List<UUID> looters = bossLooters.get(e.getMob().getUniqueId());
        for (UUID id : looters) {
            Bukkit.broadcastMessage(Bukkit.getPlayer(id).getName());
        }
    }

    // CLEAR LOOTERS LIST AFTER CHEST DESPAWNS OR AFTER 15s (restore landscape if necessary)

    /*
    A list of players who should
     */
    public List<UUID> getBossLooters(UUID bossID) {
        return bossLooters.get(bossID);
    }
}
