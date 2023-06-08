package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.PhysicalDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sprint extends Spell implements DurationSpell {
    private final Map<UUID, BukkitTask> sprintTasks = new HashMap<>();
    private double duration;
    private int level;

    public Sprint() {
        super("Sprint", CharacterClass.ROGUE);
        this.setDescription("For " + duration + "s, you gain a massive boost of speed!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        switch (level) {
            case 1 -> addStatusEffect(player, RunicStatusEffect.SPEED_I, duration, false);
            case 3 -> addStatusEffect(player, RunicStatusEffect.SPEED_III, duration, false);
            default -> addStatusEffect(player, RunicStatusEffect.SPEED_II, duration, false);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getLocation(), Color.FUCHSIA);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getEyeLocation(), Color.FUCHSIA);
        BukkitTask sprintDamageTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> sprintTasks.remove(player.getUniqueId()), (int) duration * 20L);
        sprintTasks.put(player.getUniqueId(), sprintDamageTask);
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number level = (Number) spellData.getOrDefault("level", 0);
        setLevel(level.intValue());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPhysicalDamage(PhysicalDamageEvent event) {
        if (event.isCancelled()) return;
        if (!event.isBasicAttack()) return;
        if (!sprintTasks.containsKey(event.getPlayer().getUniqueId())) return;
        Player player = event.getPlayer();
        EmpoweredSprintEvent sprintEvent = new EmpoweredSprintEvent(event.getPlayer(), event.getVictim(), event.getAmount());
        Bukkit.getPluginManager().callEvent(sprintEvent);
        if (sprintEvent.isCancelled) return;
        sprintTasks.remove(player.getUniqueId());
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * This custom event is called when the player hits a target w/ empowered spring attack
     */
    public static class EmpoweredSprintEvent extends Event implements Cancellable {
        private static final HandlerList handlers = new HandlerList();
        private final Player caster;
        private final LivingEntity victim;
        private double damage;
        private boolean isCancelled;

        /**
         * @param caster player who cast heal spell
         */
        public EmpoweredSprintEvent(Player caster, LivingEntity victim, double damage) {
            this.caster = caster;
            this.victim = victim;
            this.damage = damage;
            this.isCancelled = false;
        }

        public static HandlerList getHandlerList() {
            return handlers;
        }

        public double getDamage() {
            return damage;
        }

        public void setDamage(double damage) {
            this.damage = damage;
        }

        public LivingEntity getVictim() {
            return victim;
        }

        public Player getCaster() {
            return this.caster;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public HandlerList getHandlers() {
            return handlers;
        }

        @Override
        public boolean isCancelled() {
            return this.isCancelled;
        }

        @Override
        public void setCancelled(boolean arg0) {
            this.isCancelled = arg0;
        }
    }

}

