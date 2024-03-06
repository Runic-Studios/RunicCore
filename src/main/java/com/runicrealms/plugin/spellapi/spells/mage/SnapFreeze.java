package com.runicrealms.plugin.spellapi.spells.mage;

import com.google.common.util.concurrent.AtomicDouble;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.effect.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.mage.ChilledEffect;
import com.runicrealms.plugin.spellapi.modeled.ModeledStand;
import com.runicrealms.plugin.spellapi.modeled.StandSlot;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.TargetUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SnapFreeze extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell {
    private static final int MODEL_DATA = 2252;
    private static final double HITBOX_SCALE = 1.0;
    private static final double PERIOD = 0.5;
    private final Map<UUID, Set<UUID>> damageMap = new HashMap<>(); // Prevents concussive hits
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;
    private double stunDuration;

    public SnapFreeze() {
        super("Snap Freeze", CharacterClass.MAGE);
        this.setDescription("You cast a wave of frost in a forward line, up to " + distance + " blocks away. " +
                "Enemies hit by the spell take (" + damage + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage and are rooted for " + duration + "s! " +
                "If an enemy is &bchilled &7when hit by this spell, " +
                "consume &bchilled &7and stun them for " + stunDuration + "s instead!");
    }

    @Override
    public void loadSpellSpecificData(Map<String, Object> spellData) {
        super.loadSpellSpecificData(spellData);
        Number stunDuration = (Number) spellData.getOrDefault("stun-duration", 1.5);
        setStunDuration(stunDuration.doubleValue());
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location castLocation = player.getEyeLocation();

        freeze(player, castLocation);

        AtomicDouble count = new AtomicDouble(1);
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() > distance) {
                task.cancel();
            } else {
                count.set(count.get() + PERIOD);
                castLocation.add(castLocation.getDirection());
                freeze(player, castLocation);
            }
        }, 0, (long) PERIOD * 20L);
    }

    private void spawnSpike(Player player, Location spawnLocation) {
        player.getWorld().playSound(spawnLocation, Sound.BLOCK_GLASS_BREAK, 0.35f, 0.5f);
        new ModeledStand(
                player,
                spawnLocation,
                new Vector(0, 0, 0),
                MODEL_DATA,
                0.6,
                HITBOX_SCALE,
                StandSlot.HEAD,
                entity -> TargetUtil.isValidEnemy(player, entity)
        );
    }

    private void freeze(Player player, Location location) {
        if (!damageMap.containsKey(player.getUniqueId()))
            damageMap.put(player.getUniqueId(), new HashSet<>());
        spawnSpike(player, location);
        player.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.5f, 0.5f);
        for (Entity entity : player.getWorld().getNearbyEntities(location, HITBOX_SCALE, HITBOX_SCALE, HITBOX_SCALE, target -> TargetUtil.isValidEnemy(player, target))) {
            if (damageMap.get(player.getUniqueId()).contains(entity.getUniqueId())) continue;
            DamageUtil.damageEntitySpell(damage, (LivingEntity) entity, player, this);


            Optional<SpellEffect> spellEffectOpt = this.getSpellEffect(player.getUniqueId(), entity.getUniqueId(), SpellEffectType.CHILLED);
            if (spellEffectOpt.isPresent()) {
                ChilledEffect chilledEffect = (ChilledEffect) spellEffectOpt.get();
                chilledEffect.cancel();
                Cone.coneEffect((LivingEntity) entity, Particle.BLOCK_CRACK, stunDuration, 0, 20, Color.AQUA);
                addStatusEffect((LivingEntity) entity, RunicStatusEffect.STUN, stunDuration, true);
            } else {
                addStatusEffect((LivingEntity) entity, RunicStatusEffect.ROOT, duration, true);
            }
            damageMap.get(player.getUniqueId()).add(entity.getUniqueId());
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

    public void setStunDuration(double stunDuration) {
        this.stunDuration = stunDuration;
    }
}

