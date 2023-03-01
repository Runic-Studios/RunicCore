package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.Cone;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Random;

public class HolyNova extends Spell implements HealingSpell {
    public static final Random random = new Random(System.nanoTime());
    private static final int HEAL_AMT = 120;
    private static final int RADIUS = 6;
    private static final int SLOW_DURATION = 2;
    private static final double HEALING_PER_LEVEL = 2.0;
    /*
     * Particles to display
     */
    public int particles = 50;

    public HolyNova() {
        super("Holy Nova",
                "You charge a burst of holy energy, slowing yourself for " + SLOW_DURATION + "s. " +
                        "After, you heal✦ yourself and allies within " + RADIUS + " blocks for " +
                        "(" + HEAL_AMT + " + &f" + (int) HEALING_PER_LEVEL + "x&7 lvl) health!",
                ChatColor.WHITE, CharacterClass.CLERIC, 12, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CAMPFIRE_CRACKLE, 1.0f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1.0f, 2.0f);
        Cone.coneEffect(player, Particle.FIREWORKS_SPARK, 1, 0, 20, Color.WHITE);
        addStatusEffect(player, RunicStatusEffect.SLOW_III, SLOW_DURATION, false);
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            Location location = player.getEyeLocation();
            spawnSphere(player, location);
            for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidAlly(player, target))) {
                Player ally = (Player) entity;
                healPlayer(player, ally, HEAL_AMT, this);
            }
        }, SLOW_DURATION * 20L);
    }

    @Override
    public int getHeal() {
        return HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    private void spawnSphere(Player player, Location location) {
        for (double i = 0; i <= Math.PI; i += Math.PI / 12) {
            double radius = Math.sin(i) * HolyNova.RADIUS;
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
