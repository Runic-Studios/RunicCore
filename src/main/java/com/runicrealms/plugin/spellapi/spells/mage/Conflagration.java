package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class Conflagration extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 25;
    private static final double DAMAGE_PER_LEVEL = 0.75;
    private static final int DURATION = 4;
    private static final int PERCENT = 10;
    private static final int PERIOD = 1;
    private static final int RADIUS = 3;

    public Conflagration() {
        super("Conflagration",
                "On cast of a fire spell, you have a " +
                        PERCENT + "% chance to scorch the earth " +
                        "beneath your target, spawning an area " +
                        "of flame that deals (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies every " + PERIOD + "s. " +
                        "The conflagration lasts " + DURATION + "s.",
                ChatColor.WHITE, ClassEnum.MAGE, 0, 0);
        this.setIsPassive(true);
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
            player.getWorld().spawnParticle(Particle.FLAME, loc, 1, 0, 0, 0, 0);
            loc.subtract(x, 0, z);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Fireball
                || event.getSpell() instanceof FireAura
                || event.getSpell() instanceof FireBlast
                || event.getSpell() instanceof MeteorShower)) return; // verify fire spell
        Random rand = new Random();
        int roll = rand.nextInt(100) + 1;
        if (roll > PERCENT) return;
        spawnConflagration(event.getPlayer(), event.getVictim().getLocation(), this);
    }

    /**
     * @param caster   of the spell
     * @param location of the entity that was hit
     */
    private void spawnConflagration(Player caster, Location location, Spell spell) {
        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION) {
                    this.cancel();
                    return;
                }
                createParticleRing(caster, location, RADIUS);
                createParticleRing(caster, location, RADIUS - 1);
                createParticleRing(caster, location, RADIUS - 2);
                for (Entity entity : caster.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS)) {
                    if (!isValidEnemy(caster, entity)) continue;
                    DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) entity, caster, spell);
                    entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.5f, 1.0f);
                }
                count += 1;
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, PERIOD * 20L);
    }
}

