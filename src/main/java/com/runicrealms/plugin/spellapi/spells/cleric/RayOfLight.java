package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

@SuppressWarnings("FieldCanBeLocal")
public class RayOfLight extends Spell implements MagicDamageSpell {
    private static final int DAMAGE = 50;
    private static final int DURATION = 3;
    private static final int HEIGHT = 8;
    private static final int MAX_DIST = 8;
    private static final int RADIUS = 3;
    private static final int TRAIL_SPEED = 2;
    private static final double BEAM_WIDTH = 2.0D;
    private static final double DAMAGE_PER_LEVEL = 0.75;
    private static final double KNOCKBACK = 0.35;
    // todo: max duration

    public RayOfLight() {
        super("Ray of Light",
                "You call forth a ray of holy light that falls " +
                        "from the sky at your target enemy or location within " +
                        "8 blocks! Enemies within " + RADIUS + " blocks of the impact take (" +
                        DAMAGE + " + &f" + DAMAGE_PER_LEVEL + "x&7 lvl) magicʔ damage are " +
                        "knocked away, and are silenced for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.CLERIC, 1, 40); // todo:
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {

        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        BEAM_WIDTH,
                        entity -> isValidEnemy(player, entity)
                );

        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        }

        lightBlast(player, location);
    }

    private void explode(Player player, Location location) {
        player.getWorld().spigot().strikeLightningEffect(location, true);
        player.getWorld().playSound(location, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.5F, 1.0F);
        player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location.add(0, 1, 0), 15, 0.25f, 0, 0.25f, 0);
        for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            // Knock away
            Vector force = player.getLocation().toVector().subtract(entity.getLocation().toVector()).multiply(-KNOCKBACK).setY(0.3);
            entity.setVelocity(force);
            DamageUtil.damageEntitySpell(DAMAGE, ((LivingEntity) entity), player, this);
            addStatusEffect(entity, RunicStatusEffect.SILENCE, DURATION, true);

        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    /**
     * @param player
     * @param location
     */
    private void lightBlast(Player player, Location location) {

        final Location[] trailLoc = {location.clone().add(0, HEIGHT, 0)};
        VectorUtil.drawLine(player, Particle.SPELL_INSTANT, Color.WHITE, location, trailLoc[0].clone().subtract(0, 20, 0), 1.0D, 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (trailLoc[0].clone().subtract(0, 2, 0).getBlock().getType() != Material.AIR) { // block is on ground
                    this.cancel();
                    Bukkit.getScheduler().runTask(RunicCore.getInstance(), () -> explode(player, trailLoc[0]));
                }
                // spawn trail
                player.getWorld().playSound(trailLoc[0], Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                player.getWorld().spawnParticle(Particle.REDSTONE, trailLoc[0], 8, 0.5f, 0.5f, 0.5f, new Particle.DustOptions(Color.WHITE, 3));
                trailLoc[0] = trailLoc[0].subtract(0, TRAIL_SPEED, 0);
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 3L);
    }

}

