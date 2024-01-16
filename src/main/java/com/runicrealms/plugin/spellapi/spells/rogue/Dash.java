package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MobDamageEvent;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Dash extends Spell implements DurationSpell, PhysicalDamageSpell, RadiusSpell {
    private static final Set<Player> LUNGE_SET = new HashSet<>();
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double launchMultiplier;
    private double percent;
    private double radius;
    private double verticalPower;

    public Dash() {
        super("Dash", CharacterClass.ROGUE);
        this.setDescription("You dash forward, dealing (" + damage + " + &f" + damagePerLevel + "x&7 lvl) " +
                "physical⚔ damage to all enemies you pass through! While dashing, " +
                "you receive " + (percent * 100) + "% reduced damage from monsters!");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number launchMultiplier = (Number) spellData.getOrDefault("launch-multiplier", 1.0);
        setLaunchMultiplier(launchMultiplier.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0.75);
        setPercent(percent.doubleValue());
        Number verticalPower = (Number) spellData.getOrDefault("vertical-power", 1.0);
        setVerticalPower(verticalPower.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        // Spell variables, vectors
        Location location = player.getLocation();
        Vector look = location.getDirection();
        Vector launchPath = new Vector(look.getX(), verticalPower, look.getZ()).normalize();
        launchPath.multiply(launchMultiplier);
        double cappedY = Math.min(launchPath.getY(), 0.95);
        launchPath.setY(cappedY);
        // Particles, sounds
        addStatusEffect(player, RunicStatusEffect.SPEED_III, duration, false);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F, 1.0F);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getLocation(), Color.FUCHSIA);
        new HorizontalCircleFrame(1, false).playParticle(player, Particle.TOTEM, player.getEyeLocation(), Color.FUCHSIA);
        player.setVelocity(launchPath);
        LUNGE_SET.add(player);
        BukkitRunnable bukkitRunnable = damageNearbyEntities(player);
        bukkitRunnable.runTaskTimer(plugin, 0, 1L);
        Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                () -> {
                    LUNGE_SET.remove(player);
                    bukkitRunnable.cancel();
                }, (int) duration * 20L);
    }

    private BukkitRunnable damageNearbyEntities(Player player) {
        Spell spell = this;
        Set<Entity> damagedEntities = new HashSet<>();
        return new BukkitRunnable() {
            @Override
            public void run() {
                Collection<Entity> nearbyEntities = player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target) && !damagedEntities.contains(target));
                for (Entity entity : nearbyEntities) {
                    DamageUtil.damageEntityPhysical(damage, (LivingEntity) entity, player, false, false, spell);
                    damagedEntities.add(entity);
                }
            }
        };
    }


    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    public void setLaunchMultiplier(double launchMultiplier) {
        this.launchMultiplier = launchMultiplier;
    }

    public void setVerticalPower(double verticalPower) {
        this.verticalPower = verticalPower;
    }

    /**
     * Disable mob damage for players who are dashing
     */
    @EventHandler(priority = EventPriority.LOW)
    public void onFallDamage(MobDamageEvent event) {
        if (!(event.getVictim() instanceof Player player)) return;
        if (!LUNGE_SET.contains(player)) return;
        double damageToReduce = event.getAmount() * percent;
        event.setAmount((int) (event.getAmount() - damageToReduce));
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

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}
