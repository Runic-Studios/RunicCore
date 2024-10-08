package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HelixParticleFrame;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Map;

public class Flay extends Spell implements DistanceSpell, DurationSpell, PhysicalDamageSpell {
    public static final double BEAM_WIDTH = 2;
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;
    private double silenceDuration;

    public Flay() {
        super("Flay", CharacterClass.ROGUE);
        this.setDescription("You lash out with a phantom blade, " +
                "dealing (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage to " +
                "enemies within " + distance + " blocks and breaking their will, " +
                "slowing them for " + duration + "s. If an affected enemy is &7&obranded&7, " +
                "they are silenced for " + silenceDuration + "s!");
    }

    private void setSilenceDuration(double silenceDuration) {
        this.silenceDuration = silenceDuration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number silenceDuration = (Number) spellData.getOrDefault("silence-duration", 0);
        setSilenceDuration(silenceDuration.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.25f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITCH_THROW, 0.5f, 1.0f);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        distance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) distance).getLocation();
            location.setDirection(player.getLocation().getDirection());
            location.setY(player.getLocation().add(0, 1, 0).getY());
            flayEffect(player);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            flayEffect(player);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            for (Entity entity : player.getWorld().getNearbyEntities(livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(player, target))) {
                new HelixParticleFrame(1.0F, 30, 40.0F).playParticle(player, Particle.SOUL, entity.getLocation());
                addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_II, duration, false);
                DamageUtil.damageEntityPhysical(damage, (LivingEntity) entity, player, false, false, this);
                if (SilverBolt.getBrandedEnemiesMap().contains(entity.getUniqueId())) {
                    addStatusEffect((LivingEntity) entity, RunicStatusEffect.SILENCE, silenceDuration, true);
                }
            }
        }
    }

    private void flayEffect(Player player) {
        SlashEffect.slashVertical(player, Particle.SOUL_FIRE_FLAME, false, 0.25f);
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
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}
