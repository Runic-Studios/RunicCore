package com.runicrealms.plugin.spellapi.spells.mage;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class Blizzard extends Spell implements MagicDamageSpell {
    private static final int DAMAGE_AMOUNT = 15;
    private static final int DURATION = 5;
    private static final int MAX_DIST = 10;
    private static final int RADIUS = 3;
    private static final double DAMAGE_PER_LEVEL = 0.75;
    private static final double SNOWBALL_SPEED = 0.5;
    private static final double RAY_SIZE = 1.0D;
    private Snowball snowball;

    public Blizzard() {
        super("Blizzard",
                "You summon a cloud of snow that " +
                        "rains down snowballs for " + DURATION + " seconds, " +
                        "each dealing (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage to enemies and slowing them!",
                ChatColor.WHITE, CharacterClass.MAGE, 15, 40);
    }

    private void blizzardDamage(Player player, Location location) {
        new HorizontalCircleFrame(RADIUS, false).playParticle(player, Particle.BLOCK_CRACK, location);
        for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            player.getWorld().playSound(entity.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.25F, 1.0F);
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, (LivingEntity) entity, player, this);
            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 40, 2));
        }
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
        } else {
            location = player.getTargetBlock(null, MAX_DIST).getLocation();
        }
        spawnBlizzard(player, location);
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSnowballDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Snowball)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSnowballHit(ProjectileHitEvent event) {
        if (!event.getEntity().equals(this.snowball)) return;
        Location location = snowball.getLocation();
        snowball.remove();
        event.setCancelled(true);
        Player player = (Player) snowball.getShooter();
        if (player == null) return;
        blizzardDamage(player, location);
    }

    private void spawnBlizzard(Player player, Location location) {
        Vector launchPath = new Vector(0, -1.0, 0).multiply(SNOWBALL_SPEED);

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {
                if (count > DURATION)
                    this.cancel();
                else {
                    count++;

                    Location cloudLoc = new Location(player.getWorld(), location.getX(),
                            player.getLocation().getY(), location.getZ()).add(0, 7.5, 0);

                    // Sounds, reduced volume due to quantity of snowballs
                    player.getWorld().playSound(cloudLoc, Sound.ENTITY_ENDER_DRAGON_FLAP, 0.25f, 1.0f);
                    player.getWorld().spawnParticle(Particle.REDSTONE, cloudLoc,
                            25, 1.5f, 0.75f, 0.75f, new Particle.DustOptions(Color.WHITE, 20));

                    // Spawn 9 snowballs in a 3x3 square
                    snowball = spawnSnowball(player, cloudLoc, launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(-2, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(2, 0, 1), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -2), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 2), launchPath);
                    spawnSnowball(player, cloudLoc.add(-1, 0, 0), launchPath);
                    spawnSnowball(player, cloudLoc.add(0, 0, -2), launchPath);
                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
//                    // 1
//                    spawnSnowball(player, cloudLoc.add(-1, 0, -1), launchPath);
//                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
//                    spawnSnowball(player, cloudLoc.add(1, 0, 0), launchPath);
//                    // 2
//                    spawnSnowball(player, cloudLoc.add(1, 0, 1), launchPath);
//                    spawnSnowball(player, cloudLoc.add(0, 0, 1), launchPath);
//                    spawnSnowball(player, cloudLoc.add(0, 0, 1), launchPath);
//                    // 3
//                    spawnSnowball(player, cloudLoc.add(-1, 0, 1), launchPath);
//                    spawnSnowball(player, cloudLoc.add(-1, 0, 0), launchPath);
//                    spawnSnowball(player, cloudLoc.add(-1, 0, 0), launchPath);
//                    // 4
//                    spawnSnowball(player, cloudLoc.add(-1, 0, -1), launchPath);
//                    spawnSnowball(player, cloudLoc.add(0, 0, -1), launchPath);
//                    spawnSnowball(player, cloudLoc.add(0, 0, -1), launchPath);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, 20); // drops a snowball every second
    }

    private Snowball spawnSnowball(Player player, Location loc, Vector vec) {
        Snowball snowball = player.getWorld().spawn(loc, Snowball.class);
        snowball.setVelocity(vec);
        snowball.setShooter(player);
        return snowball;
    }
}

