package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
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
public class Rebuke extends Spell implements PhysicalDamageSpell {

    private static final int DAMAGE = 35;
    private static final double DAMAGE_PER_LEVEL = 1.75;
    private static final int DURATION = 4;
    private static final int MAX_DIST = 8;
    private static final double KNOCKUP_MULT = 1.0;
    private final double BEAM_SPEED = 0.8;
    private final double COLLISION_RADIUS = 1.5;

    public Rebuke() {
        super("Rebuke",
                "You launch a ripple of magic, colliding with the first enemy hit, " +
                        "dealing (" + DAMAGE + " + &f" + DAMAGE_PER_LEVEL +
                        "x&7 lvl) physical⚔ damage, launching them into the " +
                        "air, and slowing them for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.WARRIOR, 10, 20);
    }

    private boolean checkForEnemy(Player caster, Location beamLocation) {
        for (Entity en : caster.getWorld().getNearbyEntities(beamLocation, COLLISION_RADIUS, COLLISION_RADIUS, COLLISION_RADIUS)) {
            if (!isValidEnemy(caster, en)) continue;
            caster.getWorld().playSound(en.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 1.0f);
            knockUpParticleTask(en);
            DamageUtil.damageEntityPhysical(DAMAGE, (LivingEntity) en, caster, false, false, this);
            en.setVelocity(new Vector(0, 1, 0).normalize().multiply(KNOCKUP_MULT));
            addStatusEffect((LivingEntity) en, RunicStatusEffect.SLOW_III, DURATION, false);
            return true;
        }
        return false;
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        Location fixed = player.getLocation().clone();
        fixed.setPitch(0);
        Vector direction = fixed.getDirection().normalize().multiply(BEAM_SPEED);
        Location startLocation = player.getLocation();
        while (startLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR)
            startLocation = startLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
        Location finalBeamLocation = startLocation;

        new BukkitRunnable() {

            @Override
            public void run() {
                finalBeamLocation.add(direction);
                if (finalBeamLocation.distanceSquared(fixed) >= (MAX_DIST * MAX_DIST))
                    this.cancel();
                player.getWorld().spawnParticle(Particle.REDSTONE, finalBeamLocation,
                        15, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 3));
                if (checkForEnemy(player, finalBeamLocation))
                    this.cancel();
            }
        }.runTaskTimer(plugin, 0, 1L);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    private void knockUpParticleTask(Entity entity) {
        long startTime = System.currentTimeMillis();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (System.currentTimeMillis() - startTime > (500)) this.cancel();
                entity.getWorld().spawnParticle(Particle.REDSTONE, entity.getLocation(),
                        15, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 3));
            }
        }.runTaskTimer(plugin, 0, 1L);
    }
}

