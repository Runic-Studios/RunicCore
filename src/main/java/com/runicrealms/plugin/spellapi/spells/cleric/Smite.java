package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class Smite extends Spell {

    private static final int DAMAGE = 45;
    private static final int MAX_DIST = 10;
    private final double BEAM_SPEED = 0.8;
    private final double COLLISION_RADIUS = 1.5;
    private static final double KNOCKBACK_MULT = -2.75;

    public Smite() {
        super("Smite",
                "You launch a ripple of magic, colliding with the first enemy hit, " +
                        "dealing " + DAMAGE + " spellʔ damage and launching them back!",
                ChatColor.WHITE, ClassEnum.CLERIC, 8, 20);
    }

    @Override
    public void executeSpell(Player pl, SpellItemType type) {

        Location fixed = pl.getLocation().clone();
        fixed.setPitch(0);
        Vector direction = fixed.getDirection().normalize().multiply(BEAM_SPEED);
        Location startLocation = pl.getLocation();
        while (startLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
            startLocation = startLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();
        pl.getWorld().playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 2.0f);
        Location finalBeamLocation = startLocation;

        new BukkitRunnable() {

            @Override
            public void run() {
                finalBeamLocation.add(direction);
                if (finalBeamLocation.distanceSquared(fixed) >= (MAX_DIST * MAX_DIST))
                    this.cancel();
                pl.getWorld().spawnParticle(Particle.CLOUD, finalBeamLocation, 15, 0, 0, 0, 0);
                if (checkForEnemy(pl, finalBeamLocation))
                    this.cancel();
            }
        }.runTaskTimer(plugin, 0, 1L);
    }

    private boolean checkForEnemy(Player caster, Location beamLocation) {
        for (Entity en : caster.getWorld().getNearbyEntities(beamLocation, COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS)) {
            if (!verifyEnemy(caster, en)) continue;
            caster.getWorld().playSound(en.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.25f, 2.0f);
            en.getWorld().spawnParticle(Particle.VILLAGER_ANGRY, en.getLocation(), 15, 0.5f, 0.5f, 0.5f, 0);
            DamageUtil.damageEntitySpell(DAMAGE, (LivingEntity) en, caster, 100);
            Vector force = caster.getLocation().toVector().subtract(en.getLocation().toVector()).normalize().multiply(KNOCKBACK_MULT);
            en.setVelocity(force);
            return true;
        }
        return false;
    }
}

