package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PiercingArrow extends Spell implements DistanceSpell, PhysicalDamageSpell {
    public static final double BEAM_WIDTH = 0.5;
    private final Map<UUID, Set<UUID>> hitEntityMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double distance;

    public PiercingArrow() {
        super("Piercing Arrow", CharacterClass.ARCHER);
        this.setDescription("You launch an arrow that travels up to " +
                distance + " blocks and pierces through enemies, " +
                "dealing (" + damage + " + &f" + damagePerLevel + "x&7 lvl) physical⚔ damage " +
                "to each enemy it passes through!");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ARROW_SHOOT, 0.5f, 0.5f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.5f, 2.0f);
        Location start = player.getEyeLocation();
        Vector direction = player.getLocation().getDirection();
        double remainingDistance = distance;
        hitEntityMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        while (remainingDistance > 0) {
            RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                    (
                            start,
                            direction,
                            remainingDistance,
                            BEAM_WIDTH,
                            entity -> {
                                boolean result = isValidEnemy(player, entity);
                                if (hitEntityMap.get(player.getUniqueId()).contains(entity.getUniqueId()))
                                    result = false;
                                return result;
                            }
                    );

            if (rayTraceResult == null) {
//                Location end = player.getTargetBlock(null, (int) remainingDistance).getLocation();
                Location end = start.clone().add(direction.clone().multiply(remainingDistance));
                end.setDirection(direction);
                end.setY(start.getY());
                VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(210, 180, 140), start, end, 0.5D, 1, 0.25f);
                hitEntityMap.remove(player.getUniqueId());
                if (end.getWorld() != null) {
                    end.getWorld().spawnParticle(Particle.CRIT, end, 8, 0.8f, 0.5f, 0.8f, 0);
                }
                break;
            } else {
                LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
                if (livingEntity != null) {
                    hitEntityMap.get(player.getUniqueId()).add(livingEntity.getUniqueId());
                    VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(210, 180, 140), start, livingEntity.getEyeLocation(), 0.5D, 1, 0.25f);
                    DamageUtil.damageEntityPhysical(damage, livingEntity, player, false, true, this);
                    start = livingEntity.getEyeLocation();
                    remainingDistance -= rayTraceResult.getHitPosition().distance(start.toVector());
                }
            }
        }
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

}
