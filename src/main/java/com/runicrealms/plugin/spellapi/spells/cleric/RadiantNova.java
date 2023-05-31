package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spelltypes.WarmupSpell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class RadiantNova extends Spell implements HealingSpell, RadiusSpell, WarmupSpell {
    public static final Random random = new Random(System.nanoTime());
    /*
     * Particles to display
     */
    public int particles = 50;
    private double healAmt;
    private double healingPerLevel;
    private double radius;
    private double warmup;

    public RadiantNova() {
        super("Radiant Nova", CharacterClass.CLERIC);
        this.setDescription("You charge a burst of brilliant energy, slowing yourself for " + warmup + "s. " +
                "After, you heal✦ yourself and allies within " + radius + " blocks for " +
                "(" + healAmt + " + &f" + healingPerLevel + "x&7 lvl) health!");
    }

    private void executeHeal(Player player) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
        Location location = player.getEyeLocation();
        spawnSphere(player, location);
        for (Entity entity : player.getWorld().getNearbyEntities(location, radius, radius, radius, target -> isValidAlly(player, target))) {
            Player ally = (Player) entity;
            healPlayer(player, ally, healAmt, this);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RadiantFire radiantFire = (RadiantFire) RunicCore.getSpellAPI().getSpell("Radiant Fire");
        Map<UUID, RadiantFire.RadiantFireTask> radiantFireTaskMap = radiantFire.getRadiantFireMap();
        boolean hasWarmup = !radiantFireTaskMap.containsKey(player.getUniqueId()) || radiantFireTaskMap.get(player.getUniqueId()).getStacks().get() != radiantFire.getMaxStacks();
        if (hasWarmup) {
            player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 1.0f);
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 2.0f);
            Cone.coneEffect(player, Particle.FIREWORKS_SPARK, 1, 0, 20, Color.WHITE);
            addStatusEffect(player, RunicStatusEffect.SLOW_III, warmup, false);
            Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> executeHeal(player), (long) warmup * 20L);
        } else {
            executeHeal(player); // No warmup
        }
    }

    @Override
    public double getHeal() {
        return healAmt;
    }

    @Override
    public void setHeal(double heal) {
        this.healAmt = heal;
    }

    @Override
    public double getHealingPerLevel() {
        return healingPerLevel;
    }

    @Override
    public void setHealingPerLevel(double healingPerLevel) {
        this.healingPerLevel = healingPerLevel;
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

    private void spawnSphere(Player player, Location location) {
        for (double i = 0; i <= Math.PI; i += Math.PI / 12) {
            double radius = Math.sin(i) * this.radius;
            double y = Math.cos(i);
            for (double a = 0; a < Math.PI * 2; a += Math.PI / 12) {
                double x = .9 * Math.cos(a) * radius;
                double z = .9 * Math.sin(a) * radius;
                location.add(x, y, z);
                player.getWorld().spawnParticle(Particle.SPELL_INSTANT, location, 1, 0, 0, 0, 0);
                location.subtract(x, y, z);
            }
        }
    }

}
