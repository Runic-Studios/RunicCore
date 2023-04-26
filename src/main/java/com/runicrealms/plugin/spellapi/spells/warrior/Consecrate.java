package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spelltypes.*;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Consecrate extends Spell implements DurationSpell, MagicDamageSpell, RadiusSpell {
    private double duration;
    private double magicDamage;
    private double magicDamagePerLevel;
    private double radius;
    private double slowDuration;

    public Consecrate() {
        super("Consecrate", CharacterClass.WARRIOR);
        this.setIsPassive(true);
        this.setDescription("Your &aSlam &7spell now leaves behind " +
                "an area of consecrated ground in a " + radius + " block " +
                "radius, dealing (" + magicDamage + " + &f" + magicDamagePerLevel
                + "x&7 lvl) magicʔ damage each second for " + duration + "s " +
                "and causing enemies to receive slowness II for " + slowDuration + "s!");
    }

    private void consecrate(Player caster, @NotNull Location castLocation) {
        assert castLocation.getWorld() != null;
        Spell spell = this;
        new BukkitRunnable() {
            double count = 0;

            @Override
            public void run() {
                if (count >= duration) {
                    this.cancel();
                } else {
                    count += 1;
                    new HorizontalCircleFrame((float) radius, false).playParticle(caster, Particle.SPELL_INSTANT, castLocation, 3, Color.YELLOW);
                    for (Entity entity : castLocation.getWorld().getNearbyEntities(castLocation, radius, radius, radius, target -> isValidEnemy(caster, target))) {
                        DamageUtil.damageEntitySpell(magicDamage, (LivingEntity) entity, caster, spell);
                        addStatusEffect((LivingEntity) entity, RunicStatusEffect.SLOW_II, slowDuration, false);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
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
        Number duration = (Number) spellData.getOrDefault("duration", 0);
        setDuration(duration.doubleValue());
        Number slowDuration = (Number) spellData.getOrDefault("slow-duration", 0);
        setSlowDuration(slowDuration.doubleValue());
    }

    @Override
    public double getMagicDamage() {
        return magicDamage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.magicDamage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return magicDamagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.magicDamagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Slam)) return;
        consecrate(event.getCaster(), event.getCaster().getLocation());
    }

    public void setSlowDuration(double slowDuration) {
        this.slowDuration = slowDuration;
    }

}
