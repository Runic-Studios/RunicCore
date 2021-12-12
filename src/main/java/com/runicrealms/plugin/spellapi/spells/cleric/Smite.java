package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Smite extends Spell implements MagicDamageSpell {

    private static final int DAMAGE = 5;
    private static final double DAMAGE_PER_LEVEL = 1.5;
    private static final int MAX_DIST = 10;
    private final double BEAM_SPEED = 0.6;
    private final double COLLISION_RADIUS = 1.5;
    private static final double KNOCKBACK_MULT = -2.75;

    public Smite() {
        super("Smite",
                "You launch a ripple of magic, colliding with the first enemy hit, " +
                        "dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) spellʔ damage and launching them back!",
                ChatColor.WHITE, ClassEnum.CLERIC, 8, 20);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        Location location = player.getLocation();
        Vector direction = location.getDirection().normalize().multiply(BEAM_SPEED);
        Location startLocation = player.getEyeLocation();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);

        new BukkitRunnable() {

            @Override
            public void run() {
                startLocation.add(direction);
                if (startLocation.distanceSquared(location) >= (MAX_DIST * MAX_DIST)) {
                    player.getWorld().playSound(startLocation, Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
                    player.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, startLocation, 15, 0.5f, 0.5f, 0.5f, 0);
                    this.cancel();
                }
                player.getWorld().spawnParticle(Particle.CLOUD, startLocation, 15, 0, 0, 0, 0);
                if (checkForEnemy(player, startLocation))
                    this.cancel();
            }
        }.runTaskTimer(plugin, 0, 1L);
    }

    private boolean checkForEnemy(Player caster, Location beamLocation) {
        for (Entity en : caster.getWorld().getNearbyEntities(beamLocation, COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS)) {
            if (!verifyEnemy(caster, en)) continue;
            caster.getWorld().playSound(en.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
            en.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, en.getLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) en, caster, this);
            Vector force = caster.getLocation().toVector().subtract(en.getLocation().toVector()).normalize().multiply(KNOCKBACK_MULT);
            en.setVelocity(force);
            return true;
        }
        return false;
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }
}

