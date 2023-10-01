package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.EntityUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;

/**
 * New spell 2 for bard
 *
 * @author BoBoBalloon
 */
public class Powerslide extends Spell implements DistanceSpell, PhysicalDamageSpell, DurationSpell {
    private static final double DEGREES = Math.PI / 6;
    private double distance;
    private double damage;
    private double damagePerLevel;
    private double debuffDuration;
    private double cooldownReduction;

    public Powerslide() {
        super("Powerslide", CharacterClass.CLERIC);
        this.setDescription("You slide forward " + this.distance + " blocks. Enemies hit are pushed out of the way,\n" +
                "taking (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) physical⚔ damage and are silenced for " + this.debuffDuration + "s.\n" +
                "If you hit at least one enemy with this spell, lower its cooldown by " + this.cooldownReduction + "s.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location origin = player.getLocation().clone();
        Vector baseDirection = player.getLocation().getDirection();

        Vector direction = baseDirection.clone().setY(0).normalize().multiply(this.distance);
        player.setVelocity(player.getVelocity().add(direction));

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), task -> {
            if (origin.distance(player.getLocation()) >= this.distance) {
                player.setVelocity(new Vector(0, 0, 0));
                task.cancel();
            }
        }, 0, 1);

        List<Entity> targets = EntityUtil.getEnemiesInCone(player, (float) this.distance, DEGREES, entity -> this.isValidEnemy(player, entity));

        if (targets.isEmpty()) {
            return;
        }

        // Compute the rightward vector for later use in determining left/right
        Vector rightward = new Vector(-baseDirection.getZ(), 0, baseDirection.getX());

        for (Entity entity : targets) {
            LivingEntity target = (LivingEntity) entity;

            Vector targetVector = target.getLocation().clone().subtract(player.getLocation()).toVector().normalize();

            double dot = targetVector.dot(rightward);

            // Check the Y component of the cross product to determine the side
            if (dot > 0) {
                // Entity is to the right of the player
                entity.setVelocity(rightward.clone().multiply(3));
            } else {
                // Entity is to the left of the player
                entity.setVelocity(rightward.clone().multiply(-3));
            }

            DamageUtil.damageEntityPhysical(this.damage, target, player, false, false, false, this);
            this.addStatusEffect(target, RunicStatusEffect.SILENCE, this.debuffDuration, false);
        }

        RunicCore.getSpellAPI().reduceCooldown(player, this, this.cooldownReduction);
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number cooldownReduction = (Number) spellData.getOrDefault("cooldown-reduction", 4);
        this.cooldownReduction = cooldownReduction.doubleValue();
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
    public double getDuration() {
        return this.debuffDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.debuffDuration = duration;
    }
}
