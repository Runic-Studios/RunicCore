package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class WintersGrasp extends Spell {
    private static final int COOLDOWN = 6;
    private static final int HITS_REQUIRED = 3;
    private static final int THRESHOLD = 3;
    private static final double DURATION = 1.5D;
    private final Set<UUID> cooldownSet = new HashSet<>();
    private final ConcurrentHashMap<UUID, Map<UUID, GraspTracker>> graspTrackerMap = new ConcurrentHashMap<>();

    public WintersGrasp() {
        super("Winter's Grasp",
                "If an enemy takes " + HITS_REQUIRED + " instances of magicʔ damage " +
                        "from you within " + THRESHOLD + "s, they become frozen solid! " +
                        "Frozen enemies are stunned in place for " + DURATION + "s! " +
                        "This effect cannot occur on the same target more " +
                        "than once every " + COOLDOWN + "s.",
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSpellCast(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        // Ensure there are no null values
        UUID uuid = event.getPlayer().getUniqueId();
        UUID trackedUuid = event.getVictim().getUniqueId();
        if (cooldownSet.contains(trackedUuid)) return; // targets get locked out
        if (!graspTrackerMap.containsKey(uuid)) {
            graspTrackerMap.put(uuid, new HashMap<>());
        }
        if (!graspTrackerMap.get(uuid).containsKey(trackedUuid)) {
            GraspTracker graspTracker = new GraspTracker(uuid, event.getVictim().getUniqueId(), 0, graspTrackerMap);
            graspTrackerMap.get(uuid).put(trackedUuid, graspTracker);
        }
        // Add a stack of grasp to the uuid, refresh duration
        GraspTracker graspTracker = graspTrackerMap.get(uuid).get(trackedUuid);
        graspTracker.incrementStacks();
        graspTracker.refreshTaskDuration(graspTrackerMap);
        // If the stacks for uuid are >= threshold, do the thing and set cooldown
        if (graspTrackerMap.get(uuid).get(trackedUuid).getStacks() >= THRESHOLD) {
            graspTrackerMap.get(uuid).get(trackedUuid).close(graspTrackerMap);
            Cone.coneEffect(event.getVictim(), Particle.REDSTONE, DURATION, 0, 10L, Color.AQUA);
            addStatusEffect(event.getVictim(), RunicStatusEffect.STUN, DURATION, true);
            cooldownSet.add(trackedUuid);
            Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> cooldownSet.remove(trackedUuid), COOLDOWN * 20L);
        }
    }

    /**
     * A helper class to track the stacks of Winter's Grasp
     */
    static class GraspTracker {
        private final UUID uuid;
        private final UUID trackedUuid;
        private BukkitTask bukkitTask;
        private int stacks;

        /**
         * @param uuid            of the caster
         * @param trackedUuid     of the entity
         * @param stacks          of Winter's Grasp
         * @param graspTrackerMap the map to track all hit entities
         */
        public GraspTracker(UUID uuid, UUID trackedUuid, int stacks, Map<UUID, Map<UUID, GraspTracker>> graspTrackerMap) {
            this.uuid = uuid;
            this.trackedUuid = trackedUuid;
            this.stacks = stacks;
            this.bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> graspTrackerMap.get(uuid).remove(trackedUuid), THRESHOLD * 20L);
        }

        public void close(Map<UUID, Map<UUID, GraspTracker>> graspTrackerMap) {
            this.bukkitTask.cancel();
            graspTrackerMap.get(uuid).remove(trackedUuid);
        }

        public int getStacks() {
            return stacks;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void incrementStacks() {
            this.stacks += 1;
        }

        public void refreshTaskDuration(Map<UUID, Map<UUID, GraspTracker>> graspTrackerMap) {
            this.bukkitTask.cancel();
            this.bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> graspTrackerMap.get(uuid).remove(trackedUuid), THRESHOLD * 20L);
        }
    }


}

