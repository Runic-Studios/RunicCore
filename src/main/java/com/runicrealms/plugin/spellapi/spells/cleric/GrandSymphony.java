package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * New bard ultimate spell
 *
 * @author BoBoBalloon
 */
public class GrandSymphony extends Spell implements RadiusSpell, MagicDamageSpell, DurationSpell {
    private static final int PARTICLES_PER_RING = 15;
    private double[] ranges;
    private double radius;
    private double damage;
    private double damagePerLevel;
    private double duration;

    public GrandSymphony() {
        super("Grand Symphony", CharacterClass.CLERIC);
        this.setDescription("You pulse waves of ADJECTIVE magic every 1s for " + this.duration + "s in a " + this.radius + " block radius,\n" +
                "dealing (" + this.damage + " + &f" + this.damagePerLevel + "x&7 lvl) magicʔ damage and ADDITIONAL EFFECT HERE");
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Particle.DustOptions option = new Particle.DustOptions(Color.YELLOW, 2);

        AtomicInteger count = new AtomicInteger(1);

        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), task -> {
            if (count.get() > this.duration) {
                task.cancel();
                return;
            }

            this.particleWave(player, option);

            for (Entity entity : player.getNearbyEntities(this.radius, this.radius, this.radius)) {
                if (!(entity instanceof LivingEntity target) || !this.isValidEnemy(player, target)) {
                    continue;
                }

                DamageUtil.damageEntitySpell(this.damage, target, player, false, this);
            }

            count.set(count.get() + 1);
        }, 0, 20);
    }

    private void particleWave(@NotNull Player player, @NotNull Particle.DustOptions option) {
        AtomicInteger index = new AtomicInteger(0);

        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), particleTask -> {
            if (index.get() >= this.ranges.length) {
                particleTask.cancel();
                return;
            }

            double radius = this.ranges[index.get()];

            this.drawParticleRing(player, radius, option);

            index.set(index.get() + 1);
        }, 0, 5);
    }

    private void drawParticleRing(@NotNull Player player, double radius, @NotNull Particle.DustOptions option) {
        for (int i = 0; i < PARTICLES_PER_RING; i++) {
            double angle = (2 * Math.PI / PARTICLES_PER_RING) * i;
            double x = player.getLocation().getX() + (radius * Math.cos(angle));
            double z = player.getLocation().getZ() + (radius * Math.sin(angle));

            player.spawnParticle(Particle.REDSTONE, x, player.getLocation().getY(), z, 1, option);
        }
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
        this.ranges = IntStream.range(1, (int) radius * 2 + 1).mapToDouble(integer -> (double) integer / 2).toArray();
    }

    @Override
    public double getMagicDamage() {
        return this.damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return this.damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getDuration() {
        return this.duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }
}
