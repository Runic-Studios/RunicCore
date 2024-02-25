package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.mage.IgniteEffect;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.Map;
import java.util.Optional;

/**
 * @author BoBoBalloon, Skyfallin
 */
public class Erupt extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private static final int MAX_DIST = 10;
    private static final int MOB_DAMAGE_CAP = 500;
    private static final double RAY_SIZE = 1.5D;
    private double knockupMultiplier;
    private double damage;
    private double damagePerLevel;
    private double duration;
    private double maxHealthPercent;
    private double radius;

    public Erupt() {
        super("Erupt", CharacterClass.MAGE);
        this.setDescription("You erupt a powerful blast of fire at " +
                "your target enemy or location that deals " +
                "(" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies within " + radius + " blocks and " +
                "knocks them up! Enemies you hit are marked with fire for the next " + duration + "s. " +
                "Your &aFireball &7spell ignites this mark, dealing an additional " + (maxHealthPercent * 100) +
                "% max health magicʔ damage! " +
                "Capped at " + MOB_DAMAGE_CAP + " against monsters.");
    }

    @Override
    protected void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number knockupMultiplier = (Number) spellData.getOrDefault("knockup-multiplier", 0);
        setKnockupMultiplier(knockupMultiplier.doubleValue());
        Number maxHealthPercent = (Number) spellData.getOrDefault("max-health-percent", .05);
        setMaxHealthPercent(maxHealthPercent.doubleValue());
    }

    public void setMaxHealthPercent(double maxHealthPercent) {
        this.maxHealthPercent = maxHealthPercent;
    }

    public void setKnockupMultiplier(double knockupMultiplier) {
        this.knockupMultiplier = knockupMultiplier;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> TargetUtil.isValidEnemy(player, entity)
                );
        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        }
        fireBlast(player, location);
    }

    /**
     * Erupts a column of flame at the given location and knocks up all enemies in the radius
     *
     * @param player        who cast the spell
     * @param blastLocation to erupt the flame
     */
    private void fireBlast(Player player, Location blastLocation) {
        player.getWorld().playSound(blastLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.5f, 0.5f);
        player.getWorld().spawnParticle(Particle.LAVA, blastLocation, 25, 0.3f, 0.3f, 0.3f, 0);

        for (Entity entity : player.getWorld().getNearbyEntities(blastLocation, radius, radius, radius, target -> TargetUtil.isValidEnemy(player, target))) {
            LivingEntity livingEntity = (LivingEntity) entity;
            DamageUtil.damageEntitySpell(damage, livingEntity, player, this);
            IgniteEffect igniteEffect = new IgniteEffect(player, livingEntity, duration);
            igniteEffect.initialize();
            entity.getWorld().spawnParticle(Particle.FLAME, livingEntity.getEyeLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            entity.setVelocity(new Vector(0, 1, 0).normalize().multiply(knockupMultiplier));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private void onMagicDamage(MagicDamageEvent event) {
        if (!(event.getSpell() instanceof Fireball)) {
            return;
        }

        Optional<SpellEffect> spellEffectOpt = getSpellEffect(event.getPlayer().getUniqueId(), event.getVictim().getUniqueId(), SpellEffectType.IGNITED);
        if (spellEffectOpt.isEmpty()) return;

        IgniteEffect igniteEffect = (IgniteEffect) spellEffectOpt.get();
        igniteEffect.cancel();

        int damage = (int) (event.getVictim().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() * maxHealthPercent);
        event.getVictim().getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME, event.getVictim().getEyeLocation(), 15, 0.35f, 0.35f, 0.35f, 0);
        if (!(event.getVictim() instanceof Player)) {
            event.setAmount(event.getAmount() + Math.min(damage, 500));
        } else {
            event.setAmount(event.getAmount() + damage);
        }
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
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}

