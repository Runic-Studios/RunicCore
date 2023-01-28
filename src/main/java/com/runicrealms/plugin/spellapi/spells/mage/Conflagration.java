package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spellutil.particles.Circle;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
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
                ChatColor.WHITE, CharacterClass.MAGE, 0, 0);
        this.setIsPassive(true);
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
                || event.getSpell() instanceof FireStorm
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
                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> Circle.createParticleCircle(caster, location, RADIUS, Particle.FLAME));
                Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> Circle.createParticleCircle(caster, location, RADIUS - 2, Particle.FLAME));
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

