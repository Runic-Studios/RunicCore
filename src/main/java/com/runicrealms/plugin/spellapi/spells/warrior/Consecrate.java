package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Consecrate extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double duration;
    private double magicDamage;
    private double magicDamagePerLevel;
    private double radius;
    private double slowDuration;

    public Consecrate() {
        super("Consecrate", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Your &aSlam &7spell now leaves behind " +
                "an area of consecrated ground in a " + radius + " block " +
                "radius, dealing (" + magicDamage + " + &f" + magicDamagePerLevel
                + "x&7 lvl) magicʔ damage each second for " + duration + "s " +
                "and causing enemies to receive slowness II for " + slowDuration + "s!");
    }

    private void consecrate(Player caster, @NotNull Location castLocation) {
        assert castLocation.getWorld() != null;
        Spell spell = this;
        new BukkitRunnable() {
            double count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                } else {
                    count += 1;
                    new HorizontalCircleFrame((float) radius, false).playParticle(caster, Particle.SPELL_INSTANT, castLocation, 3, Color.YELLOW);
                    createStarParticles(castLocation, radius, Particle.VILLAGER_ANGRY);
                    for (Entity entity : castLocation.getWorld().getNearbyEntities(castLocation, radius, radius, radius, target -> isValidEnemy(caster, target))) {
                        DamageUtil.damageEntitySpell(magicDamage, (LivingEntity) entity, caster, spell);
                        addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_II, slowDuration, false);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    public void createStarParticles(Location center, double radius, Particle particle) {
        // The number of points on the star (5 for a standard star)
        int points = 5;

        // The angle between each point (in radians)
        double angleBetweenPoints = Math.PI * 2 / points;

        // The angle offset to make the star point upwards
        double angleOffset = -Math.PI / 2;

        // The ratio between the radius of the inner circle (connecting the "indents" of the star)
        // and the radius of the outer circle (connecting the points of the star)
        double innerToOuterRatio = 0.5;

        // Loop over the points of the star
        for (int i = 0; i < points * 2; i++) {
            // Alternate between the outer and inner circle
            double currentRadius = (i % 2 == 0) ? radius : radius * innerToOuterRatio;

            // Calculate the angle to the current point
            double angle = i * angleBetweenPoints / 2 + angleOffset;

            // Calculate the x and z coordinates of the current point
            double x = center.getX() + currentRadius * Math.cos(angle);
            double z = center.getZ() + currentRadius * Math.sin(angle);

            // Create a location at the current point
            Location point = new Location(center.getWorld(), x, center.getY(), z);

            // Spawn a particle at the current point
            center.getWorld().spawnParticle(particle, point, 1, 0, 0, 0, 0);
        }
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
        Number slowDuration = (Number) spellData.getOrDefault("slow-duration", 0);
        setSlowDuration(slowDuration.doubleValue());
    }

    @Override
    public double getMagicDamage() {
        return magicDamage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return magicDamagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.magicDamagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpellCast(Slam.SlamLandEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        consecrate(event.getCaster(), event.getCaster().getLocation());
    }

    public void setSlowDuration(double slowDuration) {
        this.slowDuration = slowDuration;
    }

}
