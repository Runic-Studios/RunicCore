package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.EntityUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
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
        this.setDescription("Slide forwards " + this.distance + " blocks, enemies hit are pushed out of the way,\n" +
                "take (" + this.damage + " +&f " + this.damagePerLevel + " x&7 lvl) physical⚔ damage and are silenced for " + this.debuffDuration + "s.\n" +
                "If you hit at least one enemy with this spell, lower its cooldown by " + this.cooldownReduction + "s.");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Vector direction = player.getLocation().getDirection().setY(0).multiply(this.distance);
        player.getVelocity().add(direction);

        List<Entity> targets = EntityUtil.getEnemiesInCone(player, (float) this.distance, DEGREES, entity -> this.isValidEnemy(player, entity));

        for (Entity entity : targets) {
            LivingEntity target = (LivingEntity) entity;

            Vector sidePush = new Vector(-direction.getZ(), 1, direction.getX()).normalize();
            target.setVelocity(sidePush.multiply(1.5));  // Adjust the multiplier for how strong the side push should be

            DamageUtil.damageEntitySpell(this.damage, target, player, this);
            this.addStatusEffect(target, RunicStatusEffect.SILENCE, this.debuffDuration, false);
        }

        if (targets.isEmpty()) {
            return;
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
