package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Map;

public class Discord extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell, WarmupSpell {
    private double warmup;
    private double damageAmt;
    private double damagePerLevel;
    private double duration;
    private double radius;

    public Discord() {
        super("Discord", CharacterClass.CLERIC);
        this.setDescription("You prime yourself with chaotic magic, slowing yourself for " + warmup + "s. " +
                "After, enemies within " + radius + " blocks are stunned for " +
                duration + "s and suffer (" + damageAmt + " + &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage!");
    }

    private void causeDiscord(Player caster, LivingEntity victim) {
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 1.0F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.6F);
        caster.getWorld().playSound(caster.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 0.5F, 0.2F);
        VectorUtil.drawLine(caster, Particle.CRIT_MAGIC, Color.WHITE, caster.getEyeLocation(), victim.getEyeLocation(), 1.0D, 25);
    }

    private void discord(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5f, 2.0f);
        player.getWorld().spawnParticle(Particle.CRIT_MAGIC, player.getEyeLocation(), 50, 1.0F, 0.5F, 1.0F, 0);
        for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (!(isValidEnemy(player, entity))) continue;
            causeDiscord(player, (LivingEntity) entity);
            addStatusEffect((LivingEntity) entity, RunicStatusEffect.STUN, duration, true);
            DamageUtil.damageEntitySpell(damageAmt, (LivingEntity) entity, player, this);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        addStatusEffect(player, RunicStatusEffect.SLOW_III, warmup, false);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 0.5f, 1.0f);
        Cone.coneEffect(player, Particle.NOTE, warmup, 0, 20, Color.GREEN);
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> discord(player), (long) warmup * 20L);
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
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("stun-duration", 0);
        setDuration(duration.doubleValue());
    }

    @Override
    public double getMagicDamage() {
        return damageAmt;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damageAmt = magicDamage;
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
    public double getWarmup() {
        return warmup;
    }

    @Override
    public void setWarmup(double warmup) {
        this.warmup = warmup;
    }
}
