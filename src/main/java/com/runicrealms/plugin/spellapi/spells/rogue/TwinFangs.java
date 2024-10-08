package com.runicrealms.plugin.spellapi.spells.rogue;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.SlashEffect;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

import java.util.Collection;
import java.util.Map;

public class TwinFangs extends Spell implements DistanceSpell, PhysicalDamageSpell {
    public static final double BEAM_WIDTH = 2;
    private double damage;
    private double damageCap;
    private double damagePerLevel;
    private double maxDistance;
    private double percent;

    public TwinFangs() {
        super("Twin Fangs", CharacterClass.ROGUE);
        this.setDescription("You lash out with two fangs, up to " + maxDistance + " " +
                "blocks in front of you. Each fang deals (" + damage + " + &f" +
                damagePerLevel + "x&7 lvl) physical⚔ damage on-hit, " +
                "plus an additional " + (percent * 100) + "% &cexecute &7damage! " +
                "\n\n&2&lEFFECT &cExecute" +
                "\n&7Spells with &cexecute &7deal additional damage " +
                "based on the target's missing health! " +
                "Capped at " + damageCap + " damage against monsters.");

    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number damageCap = (Number) spellData.getOrDefault("damage-cap", 500);
        setDamageCap(damageCap.doubleValue());
        Number percent = (Number) spellData.getOrDefault("percent", 0.15);
        setPercent(percent.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        traceEnemies(player, 0.75f);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> traceEnemies(player, 1.5f), 10L);
    }

    private void traceEnemies(Player player, float pitch) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, pitch);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_SPIDER_DEATH, 0.5f, pitch);
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        maxDistance,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, (int) maxDistance).getLocation();
            location.setDirection(player.getLocation().getDirection());
            location.setY(player.getLocation().add(0, 1, 0).getY());
            fangEffect(player);
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            fangEffect(player);
            livingEntity.getWorld().playSound(livingEntity.getLocation(), Sound.ENTITY_PLAYER_HURT, 0.5f, 2.0f);
            Collection<Entity> targets = player.getWorld().getNearbyEntities
                    (livingEntity.getLocation(), BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(player, target));
            targets.forEach(target -> DamageUtil.damageEntityPhysical(damage + percentMissingHealth((LivingEntity) target, percent, (int) damageCap),
                    (LivingEntity) target, player, false, false, this));
        }
    }

    /**
     * Particle for the spell
     *
     * @param player who cast the spell
     */
    private void fangEffect(Player player) {
        SlashEffect.slashVertical(player, Particle.REDSTONE, true, Color.LIME);
        SlashEffect.slashVertical(player, Particle.REDSTONE, false, Color.LIME);
    }

    @Override
    public double getDistance() {
        return maxDistance;
    }

    @Override
    public void setDistance(double distance) {
        this.maxDistance = distance;
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

    public void setDamageCap(double damageCap) {
        this.damageCap = damageCap;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }
}

