package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.EnvironmentDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LeapingShot extends Spell implements DurationSpell, PhysicalDamageSpell {
    private static final Map<UUID, Boolean> LEAP_ARROWS = new HashMap<>();
    private static final Map<UUID, BukkitTask> LEAP_TASKS = new HashMap<>();
    private final HashMap<UUID, UUID> hasBeenHit = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double launchMultiplier;
    private double verticalPower;

    public LeapingShot() {
        super("Leaping Shot", CharacterClass.ARCHER);
        this.setDescription("You fire 4 arrows in quick succession that each " +
                "(" + damage + " + &f" + damagePerLevel +
                "x &7lvl) physical⚔ damage while simultaneously leaping backwards! " +
                "During your leap, you are granted fall damage immunity for " + duration + "s.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        leap(player);

        RunicCore.getTaskChainFactory().newChain()
                .sync(() -> this.fireArrow(player, false))
                .delay(5)
                .sync(() -> this.fireArrow(player, false))
                .delay(5)
                .sync(() -> this.fireArrow(player, false))
                .delay(5)
                .sync(() -> this.fireArrow(player, true))
                .execute();
    }

    private void fireArrow(Player player, boolean last) {
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(2));
        arrow.setShooter(player);
        arrow.setCustomNameVisible(false);
        LEAP_ARROWS.put(arrow.getUniqueId(), last);
        arrow.setBounce(false);
        EntityTrail.entityTrail(arrow, Particle.CRIT);
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
        Number launchMultiplier = (Number) spellData.getOrDefault("launch-multiplier", 0);
        setLaunchMultiplier(launchMultiplier.doubleValue());
        Number verticalPower = (Number) spellData.getOrDefault("vertical-power", 0);
        setVerticalPower(verticalPower.doubleValue());
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    /**
     * Performs the lunge effect, and caps the Y component of the vector to prevent odd behavior
     *
     * @param player who cast the spell
     */
    public void leap(Player player) {
        // Spell variables, vectors
        Location location = player.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(-look.getX(), verticalPower, -look.getZ()).normalize();
        launchPath.multiply(launchMultiplier);
        double cappedY = Math.min(launchPath.getY(), 0.95);
        launchPath.setY(cappedY);
        // Particles, sounds
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.5f, 1.2f);
        player.getWorld().spawnParticle(Particle.REDSTONE, player.getLocation(),
                25, 0.5f, 0.5f, 0.5f, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 20));
        player.setVelocity(launchPath);
        BukkitTask lungeDamageTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> LEAP_TASKS.remove(player.getUniqueId()), (int) duration * 20L);
        LEAP_TASKS.put(player.getUniqueId(), lungeDamageTask);
    }

    /**
     * Disable fall damage for players who are lunging
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(EnvironmentDamageEvent event) {
        if (!LEAP_TASKS.containsKey(event.getVictim().getUniqueId())) return;
        if (event.getCause() == EnvironmentDamageEvent.DamageCauses.FALL_DAMAGE)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPhysicalDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getDamager() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof Player player)) return;

        Boolean last = LEAP_ARROWS.remove(arrow.getUniqueId());

        if (last == null) {
            return;
        }

        if (hasBeenHit.containsKey(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity livingEntity)) return;

        event.setCancelled(true);
        Bukkit.getPluginManager().callEvent(new ArrowHitEvent(player, livingEntity, last));
        DamageUtil.damageEntityPhysical(damage, livingEntity, player, false, true, this);
        hasBeenHit.put(player.getUniqueId(), livingEntity.getUniqueId()); // prevent concussive hits
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> hasBeenHit.remove(player.getUniqueId()), 8L);
    }

    public void setLaunchMultiplier(double launchMultiplier) {
        this.launchMultiplier = launchMultiplier;
    }

    public void setVerticalPower(double verticalPower) {
        this.verticalPower = verticalPower;
    }

    /**
     * An event that is called before any spell damage is done to an enemy when they are hit with a leaping shot arrow
     *
     * @author BoBoBalloon
     */
    public static class ArrowHitEvent extends Event {
        private final Player caster;
        private final LivingEntity victim;
        private final boolean last;

        private static final HandlerList HANDLER_LIST = new HandlerList();

        public ArrowHitEvent(@NotNull Player caster, @NotNull LivingEntity victim, boolean last) {
            this.caster = caster;
            this.victim = victim;
            this.last = last;
        }

        @NotNull
        public Player getCaster() {
            return this.caster;
        }

        @NotNull
        public LivingEntity getVictim() {
            return this.victim;
        }

        public boolean isLast() {
            return this.last;
        }

        @NotNull
        @Override
        public HandlerList getHandlers() {
            return HANDLER_LIST;
        }

        @NotNull
        public static HandlerList getHandlerList() {
            return HANDLER_LIST;
        }
    }
}
