package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class Ruination extends Spell implements DurationSpell, HealingSpell, MagicDamageSpell, RadiusSpell {
    private double baseDuration;
    private double damage;
    private double damagePerLevel;
    private double durationExtension;
    private double heal;
    private double maxDuration;
    private double maxTargets;
    private double radius;
    private double healingPerLevel;

    public Ruination() {
        super("Ruination", CharacterClass.CLERIC);
        this.setDescription("For the next " + baseDuration + "s, a "
                + radius + " block radius around you " +
                "becomes a realm of death! Up to " + maxTargets + " enemies within the field " +
                "take (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage per second! " +
                "Additionally, every time this spell deals damage to an enemy, " +
                "you heal✦ for (" + heal + " + &f" + healingPerLevel + "x&7 lvl) " +
                "health per enemy and " +
                "extend the duration of this spell by " + durationExtension + "s, " +
                "up to a max of " + maxDuration + "s!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getEyeLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.5F, 1.0F);
        final int[] duration = {(int) baseDuration};
        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > duration[0]) {
                    this.cancel();
                } else {
                    count += 1;
                    player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_GENERIC_DRINK, 1.0F, 2.0F);
                    boolean incrementDuration = false;
                    int targets = 0;
                    new HorizontalCircleFrame((float) radius, false).playParticle(player, Particle.SOUL, player.getEyeLocation());
                    for (Entity entity : player.getWorld().getNearbyEntities
                            (player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
                        if (targets >= maxTargets) break;
                        incrementDuration = true;
                        targets++;
                        DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, spell);
                        healPlayer(player, player, heal, spell);
                    }
                    if (incrementDuration && duration[0] < maxDuration) {
                        duration[0] += 1;
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    public double getDamage() {
        return damage;
    }

    public void setDamage(double damage) {
        this.damage = damage;
    }

    public double getDamagePerLevel() {
        return damagePerLevel;
    }

    public void setDamagePerLevel(double damagePerLevel) {
        this.damagePerLevel = damagePerLevel;
    }

    @Override
    public double getDuration() {
        return baseDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.baseDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number baseDuration = (Number) spellData.getOrDefault("base-duration", 0);
        setDuration(baseDuration.doubleValue());
        Number durationExtension = (Number) spellData.getOrDefault("duration-extension", 0);
        setDurationExtension(durationExtension.doubleValue());
        Number maxDuration = (Number) spellData.getOrDefault("max-duration", 0);
        setMaxDuration(maxDuration.doubleValue());
        Number maxTargets = (Number) spellData.getOrDefault("max-targets", 0);
        setMaxTargets(maxTargets.doubleValue());
    }

    public double getDurationExtension() {
        return durationExtension;
    }

    public void setDurationExtension(double durationExtension) {
        this.durationExtension = durationExtension;
    }

    @Override
    public double getHeal() {
        return heal;
    }

    @Override
    public void setHeal(double heal) {
        this.heal = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
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

    public double getMaxDuration() {
        return maxDuration;
    }

    public void setMaxDuration(double maxDuration) {
        this.maxDuration = maxDuration;
    }

    public double getMaxTargets() {
        return maxTargets;
    }

    public void setMaxTargets(double maxTargets) {
        this.maxTargets = maxTargets;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }
}

