package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.EntityTrail;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class MeteorShower extends Spell implements MagicDamageSpell {

    private static final int AMOUNT = 4;
    private static final int DAMAGE_AMOUNT = 35;
    private static final int HEIGHT = 8;
    private static final int MAX_DIST = 12;
    private static final int RADIUS = 4;
    private static final double FIREBALL_SPEED = 1.25D;
    private static final double DAMAGE_PER_LEVEL = 0.85;
    private static final double RAY_SIZE = 1.0D;
    private LargeFireball meteor;

    public MeteorShower() {
        super("Meteor Shower",
                "You conjure a shower of meteors at your target " +
                        "enemy or location within " + MAX_DIST + " blocks!" +
                        "Four projectile meteors rain from the shower that deal " +
                        "(" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies within " + RADIUS + " blocks on impact!",
                ChatColor.WHITE, CharacterClass.MAGE, 10, 25);
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                (
                        player.getLocation(),
                        player.getLocation().getDirection(),
                        MAX_DIST,
                        RAY_SIZE,
                        entity -> isValidEnemy(player, entity)
                );
        Location location;
        if (rayTraceResult == null) {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        } else if (rayTraceResult.getHitEntity() != null) {
            location = rayTraceResult.getHitEntity().getLocation();
        } else if (rayTraceResult.getHitBlock() != null) {
            location = rayTraceResult.getHitBlock().getLocation();
//            Block block = rayTraceResult.getHitBlock();
//            while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR)
//                block = block.getRelative(BlockFace.DOWN);
//            location = block.getLocation();
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
//            Block block = player.getTargetBlock(null, MAX_DIST);
//            while (block.getRelative(BlockFace.DOWN).getType() == Material.AIR)
//                block = block.getRelative(BlockFace.DOWN);
//            location = block.getLocation();
        }
        summonMeteorShower(player, location);
    }

    /**
     * @param player
     * @param location
     */
    private void explode(Player player, Location location) {
        new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.FLAME, location, Color.RED);
        player.getWorld().playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 0.5F, 1.0F);
        player.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 15, 0.25f, 0, 0.25f, 0);
        for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, (LivingEntity) entity, player, this);
            player.getWorld().playSound(location, Sound.ENTITY_PLAYER_HURT, 0.5f, 1);
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (event.getEntity() instanceof LargeFireball) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onFireballDamage(ProjectileHitEvent event) {
        if (!event.getEntity().equals(meteor)) return;
        Location location = meteor.getLocation();
        meteor.remove();
        event.setCancelled(true);
        Player player = (Player) meteor.getShooter();
        if (player == null) return;
        explode(player, location);
    }

    /**
     * @param player
     * @param location
     */
    private void summonMeteorShower(Player player, Location location) {
        final Location[] meteorLocation = {location.clone().add(0, HEIGHT, 0)};
        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count >= AMOUNT) {
                    this.cancel();
                } else {
                    count += 1;
                    meteor = (LargeFireball) player.getWorld().spawnEntity(meteorLocation[0], EntityType.FIREBALL);
                    EntityTrail.entityTrail(meteor, Particle.FLAME);
                    meteor.setIsIncendiary(false);
                    meteor.setYield(0F);
                    final Vector velocity = new Vector(0, -1, 0).multiply(FIREBALL_SPEED);
                    meteor.setVelocity(velocity);
                    meteor.setShooter(player);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.2f);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0L, 20L);
    }
}

