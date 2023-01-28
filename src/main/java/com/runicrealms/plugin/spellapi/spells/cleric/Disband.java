package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Disband extends Spell {
    private static final int ATTACKS_TO_TRIGGER = 3;
    private static final int COOLDOWN = 7;
    private static final int DURATION = 3;
    private static final int THRESHOLD = 10;
    private final Set<UUID> cooldownSet = new HashSet<>();
    private final ConcurrentHashMap<UUID, DisbandTracker> bardsMap = new ConcurrentHashMap<>();

    public Disband() {
        super("Disband",
                "Damaging the same enemy " + ATTACKS_TO_TRIGGER + " times with basic attacks " +
                        "within " + THRESHOLD + "s causes you to disarm the enemy for " + DURATION + "s! " +
                        "Disarmed enemies are unable to deal damage with basic attacks. " +
                        "Damaging a different enemy during this time resets the counter. " +
                        "This effect cannot occur more than once every " + COOLDOWN + "s. ",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler
    public void onBasicAttack(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (cooldownSet.contains(event.getPlayer().getUniqueId())) return;
        // add to counter
        UUID uuid = event.getPlayer().getUniqueId();
        if (!bardsMap.containsKey(uuid)) {
            bardsMap.put(uuid, new DisbandTracker(uuid, event.getVictim().getUniqueId(), 0, bardsMap));
        }
        UUID trackedUuid = bardsMap.get(uuid).getTrackedUuid(); // the tracked entity
        if (event.getVictim().getUniqueId() != trackedUuid) { // player hit a new entity
            bardsMap.get(uuid).setTrackedUuid(trackedUuid);
            bardsMap.get(uuid).setStacks(0); // reset stacks
        }
        bardsMap.get(uuid).setStacks(bardsMap.get(uuid).getStacks() + 1);
        event.getVictim().getWorld().spawnParticle
                (Particle.VILLAGER_ANGRY, event.getVictim().getLocation().add(0, 1.5, 0),
                        5, 1.0F, 0, 0, 0);

        if (bardsMap.get(uuid).getStacks() >= ATTACKS_TO_TRIGGER) {
            addStatusEffect(event.getVictim(), RunicStatusEffect.DISARM, DURATION, true);
            bardsMap.get(uuid).getBukkitTask().cancel();
            bardsMap.remove(uuid);
            cooldownSet.add(event.getPlayer().getUniqueId());
            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> cooldownSet.remove(uuid), COOLDOWN * 20L);
        }
    }

    static class DisbandTracker {
        private final UUID uuid;
        private final BukkitTask bukkitTask;
        private UUID trackedUuid;
        private int stacks;

        public DisbandTracker(UUID uuid, UUID trackedUuid, int stacks, Map<UUID, DisbandTracker> bardsMap) {
            this.uuid = uuid;
            this.trackedUuid = trackedUuid;
            this.stacks = stacks;
            this.bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> bardsMap.remove(uuid), THRESHOLD * 20L);
        }

        public BukkitTask getBukkitTask() {
            return bukkitTask;
        }

        public int getStacks() {
            return stacks;
        }

        public void setStacks(int stacks) {
            this.stacks = stacks;
        }

        public UUID getTrackedUuid() {
            return trackedUuid;
        }

        public void setTrackedUuid(UUID trackedUuid) {
            this.trackedUuid = trackedUuid;
        }

        public UUID getUuid() {
            return uuid;
        }
    }

}

