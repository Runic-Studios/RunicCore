package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("FieldCanBeLocal")
public class Consecration extends Spell implements MagicDamageSpell {

    private static final int DAMAGE_AMT = 5;
    private static final double DAMAGE_PER_LEVEL = 0.25;
    private static final int DURATION = 8;
    private static final int RADIUS = 7;

    public Consecration() {
        super("Consecration",
                "You conjure a ring of holy magic on the ground " +
                        "for " + DURATION + "s, slowing enemies within " + RADIUS + " " +
                        "blocks and dealing (" + DAMAGE_AMT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage each second!",
                ChatColor.WHITE, ClassEnum.CLERIC, 15, 20);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location castLocation = player.getLocation();
        Spell spell = this;

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                } else {
                    createParticleRing(player, castLocation, RADIUS);
                    createParticleRing(player, castLocation, RADIUS - 2);
                    createParticleRing(player, castLocation, RADIUS - 4);
                    player.getWorld().playSound(castLocation, Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 2.0f);
                    for (Entity en : player.getWorld().getNearbyEntities(castLocation, RADIUS, RADIUS, RADIUS)) {
                        if (!(isValidEnemy(player, en))) continue;
                        LivingEntity victim = (LivingEntity) en;
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 2));
                        DamageUtil.damageEntitySpell(DAMAGE_AMT, victim, player, spell);
                    }
                    count += 1;
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20L);
    }

    /**
     * Creates a ring of particles around the given location, spawned in the player's world, with the given radius
     *
     * @param player who summoned the particles
     * @param loc    around which to build the ring
     * @param radius of the circle
     */
    private void createParticleRing(Player player, Location loc, int radius) {
        int particles = 50;
        for (int i = 0; i < particles; i++) {
            double angle, x, z;
            angle = 2 * Math.PI * i / particles;
            x = Math.cos(angle) * (float) radius;
            z = Math.sin(angle) * (float) radius;
            loc.add(x, 0, z);
            player.getWorld().spawnParticle(Particle.SPELL_INSTANT, loc, 1, 0, 0, 0, 0);
            player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, 0, 0, 0, 0, new Particle.DustOptions(Color.WHITE, 1));
            loc.subtract(x, 0, z);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}
