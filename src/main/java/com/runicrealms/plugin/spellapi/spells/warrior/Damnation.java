package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.spellapi.spellutil.particles.ParticleSphere;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Map;

public class Damnation extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell, WarmupSpell {
    private double damage;
    private double damagePerLevel;
    private double damagePerSouls;
    private double duration;
    private double multiplier;
    private double radius;
    private double radiusPerSouls;
    private double warmup;

    public Damnation() {
        super("Damnation", CharacterClass.WARRIOR);
        this.setDescription("You prime yourself with unholy magic, slowing yourself for " + warmup + "s. " +
                "Then, you consume all of your &3souls &7to get an aura around you " +
                "that lasts " + duration + "s! The aura’s radius is equal to " +
                "(" + radius + " + &f" + radiusPerSouls
                + "x&7 &3souls&7) and its magicʔ damage is (" + damage + " + &f" + damagePerSouls
                + "x&7 &3souls&7)! " +
                "Enemies within the aura are slowed and pulled " +
                "towards you each second while the aura persists!");
    }

    @Override
    public void loadRadiusData(Map<String, Object> spellData) {
        Number radius = (Number) spellData.getOrDefault("radius", 0);
        setRadius(radius.doubleValue());
        Number radiusPerSouls = (Number) spellData.getOrDefault("radius-per-souls", 0);
        setRadiusPerSouls(radiusPerSouls.doubleValue());
    }

    @Override
    public void loadMagicData(Map<String, Object> spellData) {
        Number magicDamage = (Number) spellData.getOrDefault("magic-damage", 0);
        setMagicDamage(magicDamage.doubleValue());
        Number magicDamagePerLevel = (Number) spellData.getOrDefault("magic-damage-per-level", 0);
        setMagicDamagePerLevel(magicDamagePerLevel.doubleValue());
        Number magicDamagePerSouls = (Number) spellData.getOrDefault("magic-damage-per-souls", 0);
        setDamagePerSouls(magicDamagePerSouls.doubleValue());
        Number multiplier = (Number) spellData.getOrDefault("multiplier", 0);
        setMultiplier(multiplier.doubleValue());
    }

    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    private void damnation(Player player) {
        Spell spell = this;
        int souls = SoulReaper.getReaperTaskMap().get(player) != null ? SoulReaper.getReaperTaskMap().get(player).getStacks().get() : 0;
        double bonusDamage = damagePerSouls * souls;
        double totalDamage = damage + bonusDamage;
        double bonusRadius = radiusPerSouls * souls;
        double totalRadius = radius + bonusRadius;
        // Cancel the future task to reset souls
        if (SoulReaper.getReaperTaskMap().containsKey(player))
            SoulReaper.getReaperTaskMap().get(player).reset(0, () -> {
            });
        // Manually reset souls
        SoulReaper.cleanupTask(player, "Soul Reaper has been consumed!");
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {

                count += 0.25;
                if (count > duration)
                    this.cancel();

                if (count % 1 == 0) {
                    new ParticleSphere
                            (
                                    player,
                                    Sound.BLOCK_LAVA_EXTINGUISH,
                                    totalRadius,
                                    Color.fromRGB(185, 251, 185),
                                    50
                            ).show();
                }

                for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), totalRadius, totalRadius, totalRadius, target -> isValidEnemy(player, target))) {
                    if (count % 1 == 0) {
                        DamageUtil.damageEntitySpell(totalDamage, (LivingEntity) entity, player, spell);
                    }

                    // Pull to middle
                    Vector directionToMiddle = player.getLocation().subtract(entity.getLocation()).toVector();
                    if (directionToMiddle.lengthSquared() > 0) { // Check if the vector is not zero
                        directionToMiddle.setY(0);
                        directionToMiddle.normalize().multiply(multiplier); // Adjust this value to change the strength of the pull
                        entity.setVelocity(directionToMiddle);
                    }

                    addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_II, 1, false);
                }
            }
        }.runTaskTimer(plugin, 0, 5L);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        addStatusEffect(player, RunicStatusEffect.SLOW_III, warmup, false);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.SLIME, warmup, 0, 20, Color.GREEN);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> damnation(player), (long) warmup * 20L);
    }

    public void setDamagePerSouls(double damagePerSouls) {
        this.damagePerSouls = damagePerSouls;
    }

    public void setRadiusPerSouls(double radiusPerSouls) {
        this.radiusPerSouls = radiusPerSouls;
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
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @Override
    public double getWarmup() {
        return warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }
}

