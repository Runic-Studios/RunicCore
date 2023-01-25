package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Disband extends Spell {
    private static final int ATTACKS_TO_TRIGGER = 3;
    private static final int COOLDOWN = 7;
    private static final int DURATION = 3;
    private static final int THRESHOLD = 10;
    private final Set<UUID> cooldownSet = new HashSet<>();
    private final Map<UUID, DisbandTracker> bardsMap = new HashMap<>();

    public Disband() {
        super("Disband",
                "Damaging the same enemy " + ATTACKS_TO_TRIGGER + " times with basic attacks " +
                        "within " + THRESHOLD + "s causes you to disarm the enemy for " + DURATION + "s! " +
                        "Disarmed enemies are unable to deal damage with basic attacks. " +
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
        bardsMap.computeIfAbsent(uuid, key -> bardsMap.put(key, new DisbandTracker(key, event.getVictim().getUniqueId(), 0, bardsMap)));
        UUID trackedUuid = bardsMap.get(uuid).getTrackedUuid(); // the tracked entity
        if (event.getVictim().getUniqueId() != trackedUuid) { // player hit a new entity
            bardsMap.get(uuid).setStacks(bardsMap.get(uuid).getStacks() + 1);
        }
        // todo: angry villager
//        bardsMap.get(uuid).second
//
//        int count = bardsMap.get(uuid);
//        if (count >= ATTACKS_TO_TRIGGER) {
//            // todo: effect
//        }
    }

    static class DisbandTracker {
        private final UUID uuid;
        private final UUID trackedUuid;
        private final BukkitTask bukkitTask;
        private int stacks;

        public DisbandTracker(UUID uuid, UUID trackedUuid, int stacks, Map<UUID, DisbandTracker> bardsMap) {
            this.uuid = uuid;
            this.trackedUuid = trackedUuid;
            this.stacks = stacks;
            this.bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> bardsMap.remove(uuid), THRESHOLD * 20L);
        }

        public UUID getUuid() {
            return uuid;
        }

        public UUID getTrackedUuid() {
            return trackedUuid;
        }

        public int getStacks() {
            return stacks;
        }

        public void setStacks(int stacks) {
            this.stacks = stacks;
        }

        public BukkitTask getBukkitTask() {
            return bukkitTask;
        }
    }

}

