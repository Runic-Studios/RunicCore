package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.BleedEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * New spell 1 for berserker
 *
 * @author BoBoBalloon
 */
public class Cleave extends Spell implements DistanceSpell, DurationSpell, PhysicalDamageSpell, RadiusSpell {
    private static final double ANGLE_DEGREES = 180;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double distance;
    private double radius;
    private double tick;

    public Cleave() {
        super("Cleave", CharacterClass.WARRIOR);
        this.setDescription("You brutally slash around yourself, dealing (" + this.damage +
                " + &f" + this.damagePerLevel + "x&7 lvl) physical⚔ damage every " +
                this.tick + "s for " + this.duration + "s! " +
                "The final slash causes enemies to &cbleed&7!" +
                "\n\n&2&lEFFECT &cBleed" +
                "\n&cBleeding &7enemies take 3% max health physical⚔ damage every 2.0s for 6.0s. " +
                "(Capped at " + BleedEffect.DAMAGE_CAP + " damage). " +
                "During this time, enemy players receive " + (BleedEffect.HEALING_REDUCTION * 100) + "% less healing.");
    }

    private void cleaveEffect(Player player, int count) {
        double maxAngleCos = Math.cos(Math.toRadians(ANGLE_DEGREES));
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.75f, 0.5f);
        // Damage entities in front of the player
        SlashEffect.slashHorizontal(player.getLocation());
        for (Entity entity : player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius, target -> isValidEnemy(player, target))) {
            Location entityLocation = entity.getLocation();
            Vector directionToEntity = entityLocation.subtract(player.getLocation()).toVector().normalize();
            // Check if the entity is in front of the player (cosine of the angle between the vectors > 0)
            double dot = player.getLocation().getDirection().dot(directionToEntity);
            if (dot < maxAngleCos) continue;
            DamageUtil.damageEntityPhysical(this.damage, (LivingEntity) entity, player, false, false, this);
            if (count >= this.duration - 1) {
                Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), entity.getUniqueId(), SpellEffectType.BLEED);
                if (spellEffectOpt.isEmpty()) {
                    BleedEffect bleedEffect = new BleedEffect(player, (LivingEntity) entity);
                    bleedEffect.initialize();
                }
            }
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        AtomicInteger count = new AtomicInteger(0);
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() >= this.duration) {
                task.cancel();
                return;
            }
            cleaveEffect(player, count.get());
            count.getAndIncrement();
        }, 0, (long) (this.tick * 20));
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number tick = (Number) spellData.getOrDefault("tick", 1);
        this.tick = tick.doubleValue();
    }

    @Override
    public double getDistance() {
        return this.distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @Override
    public double getPhysicalDamage() {
        return this.damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return this.damagePerLevel;
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
}
