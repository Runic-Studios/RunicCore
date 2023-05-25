package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;

public class Condemn extends Spell implements DistanceSpell, DurationSpell, PhysicalDamageSpell {
    public static final double BEAM_WIDTH = 2;
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;

    public Condemn() {
        super("Condemn", CharacterClass.ROGUE);
        this.setDescription("You lash out with your blade, " +
                "dealing (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage to " +
                "enemies within " + distance + " blocks and breaking their will, " +
                "slowing them for " + duration + "s. If an affected enemy is &7&obranded&7, " +
                "they are grounded for the duration!");
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
            condemnEffect(player);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            condemnEffect(player);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(player, target));
            targets.forEach(target -> {
                addStatusEffect((LivingEntity) target, RunicStatusEffect.SLOW_II, duration, false);
                DamageUtil.damageEntityPhysical(damage,
                        (LivingEntity) target, player, false, false, this);
                if (SilverBolt.getBrandedEnemiesMap().contains(target.getUniqueId())) {
                    ((LivingEntity) target).addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (int) (duration * 20L), -999));
                }

            });
        }
    }

    private void condemnEffect(Player player) {
        SlashEffect.slashVertical(player, Particle.REDSTONE, false, Color.WHITE);
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
