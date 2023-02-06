package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

public class Remedy extends Spell implements HealingSpell {

    public static final Random random = new Random(System.nanoTime());
    private static final int DURATION = 2;
    private static final int RADIUS = 5;
    private static final double HEAL_AMT = 20.0;
    private static final double HEALING_PER_LEVEL = 1.0;
    /*
     * Particles to display
     */
    public int particles = 50;

    public Remedy() {
        super("Remedy",
                "You and allies within " + RADIUS +
                        " blocks are cleansed of slow effects and healed✦ for (" +
                        (int) HEAL_AMT + " + &f" + (int) HEALING_PER_LEVEL + "x&7 lvl) health over " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.ARCHER, 8, 20);
    }

    private void createSphere(Player player, Location loc) {
        for (int i = 0; i < particles; i++) {
            Vector vector = getRandomVector().multiply(RADIUS);
            loc.add(vector);
            player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1, 0, 0, 0, 0);
            loc.subtract(vector);
        }
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Spell spell = this;
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    count += 1;

                    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRASS_STEP, 0.5f, 0.2f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PARROT_AMBIENT, 0.5f, 0.2f);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.25f, 0.2f);
                    createSphere(player, player.getEyeLocation());
                    healPlayer(player, player, HEAL_AMT, spell);
                    for (PotionEffect effect : player.getActivePotionEffects()) {
                        if (effect.getType() == PotionEffectType.SLOW)
                            player.removePotionEffect(effect.getType());
                    }
                    for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
                        if (entity.equals(player)) continue;
                        if (!isValidAlly(player, entity)) continue;
                        Player playerEntity = (Player) entity;
                        for (PotionEffect effect : playerEntity.getActivePotionEffects()) {
                            if (effect.getType() == PotionEffectType.SLOW)
                                playerEntity.removePotionEffect(effect.getType());
                        }
                        healPlayer(player, playerEntity, HEAL_AMT / DURATION, spell);
                    }
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);

    }

    @Override
    public int getHeal() {
        return (int) HEAL_AMT;
    }

    @Override
    public double getHealingPerLevel() {
        return HEALING_PER_LEVEL;
    }

    private Vector getRandomVector() {
        double x, y, z;
        x = random.nextDouble() * 2 - 1;
        y = random.nextDouble() * 2 - 1;
        z = random.nextDouble() * 2 - 1;
        return new Vector(x, y, z).normalize();
    }

}
