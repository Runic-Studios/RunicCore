package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Shockwave extends Spell implements DistanceSpell, PhysicalDamageSpell {
    private static final double BEAM_SPEED = 0.4; // 0.8
    private static final double BEAM_WIDTH = 0.5;
    private final Map<UUID, Set<UUID>> hitEntityMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double knockupMultiplier;

    public Shockwave() {
        super("Shockwave", CharacterClass.WARRIOR);
        this.setDescription("You smash your foot down, sending out a " +
                "shockwave in a line in front of you, knocking up " +
                "all enemies hit and dealing (" + damage + " + &f" + damagePerLevel +
                "x&7 lvl) physical⚔ damage! " +
                "Mobs hit by Shockwave are taunted, causing them to attack you.");
    }

    @Override
    public void loadDistanceData(Map<String, Object> spellData) {
        Number distance = (Number) spellData.getOrDefault("distance", 0);
        setDistance(distance.doubleValue());
        Number knockupMultiplier = (Number) spellData.getOrDefault("knockup-multiplier", 0);
        setKnockupMultiplier(knockupMultiplier.doubleValue());
    }

    private void setKnockupMultiplier(double knockupMultiplier) {
        this.knockupMultiplier = knockupMultiplier;
    }

//    private void checkForEnemies(Player caster, Location beamLocation) {
//        for (Entity entity : beamLocation.getWorld().getNearbyEntities(beamLocation, BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH, target -> isValidEnemy(caster, target))) {
//            Bukkit.broadcastMessage(ChatColor.RED + "entity");
//            if (hitEntityMap.get(caster.getUniqueId()).contains(entity.getUniqueId()))
//                continue;
//            Bukkit.broadcastMessage(ChatColor.GREEN + "damage here");
//            caster.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 0.25f);
//            DamageUtil.damageEntityPhysical(damage, (LivingEntity) entity, caster, false, false, this);
//            entity.setVelocity(new Vector(0, 1, 0).normalize().multiply(knockupMultiplier));
//            hitEntityMap.get(caster.getUniqueId()).add(entity.getUniqueId());
//        }
//    }


    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_STONE_BREAK, 0.5f, 1.0f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GRAVEL_BREAK, 1.0f, 0.25f);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.25f);
        Vector direction = player.getLocation().getDirection();
        double remainingDistance = distance;
        hitEntityMap.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());
        Location startLocation = player.getLocation();
        while (startLocation.getY() > 0 && startLocation.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            startLocation = startLocation.getBlock().getRelative(BlockFace.DOWN).getLocation();
        }
//        new ShockwaveEffect(player, startLocation).runTaskTimer(RunicCore.getInstance(), 0, 1);
        while (remainingDistance > 0) {
            RayTraceResult rayTraceResult = player.getWorld().rayTraceEntities
                    (
                            startLocation,
                            direction,
                            remainingDistance,
                            BEAM_WIDTH,
                            entity -> {
                                boolean result = isValidEnemy(player, entity);
                                if (hitEntityMap.get(player.getUniqueId()).contains(entity.getUniqueId()))
                                    result = false;
                                return result;
                            }
                    );

            if (rayTraceResult == null) {
                Location end = startLocation.clone().add(direction.clone().multiply(remainingDistance));
                end.setDirection(direction);
                end.setY(startLocation.getY());

                // Ensure that the end location is on the ground.
                while (end.getY() > 0 && end.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
                    end = end.getBlock().getRelative(BlockFace.DOWN).getLocation();
                }
                
                VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(210, 180, 140), startLocation, end, 0.5D, 1, 0.25f);
                hitEntityMap.remove(player.getUniqueId());
                if (end.getWorld() != null) {
                    end.getWorld().spawnParticle(Particle.CRIT, end, 8, 0.8f, 0.5f, 0.8f, 0);
                }
                break;
            } else {
                LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
                if (livingEntity != null) {
                    hitEntityMap.get(player.getUniqueId()).add(livingEntity.getUniqueId());
                    VectorUtil.drawLine(player, Particle.REDSTONE, Color.fromRGB(210, 180, 140), startLocation, livingEntity.getEyeLocation(), 0.5D, 1, 0.25f);
                    DamageUtil.damageEntityPhysical(damage, livingEntity, player, false, true, this);
                    livingEntity.setVelocity(new Vector(0, 1, 0).normalize().multiply(knockupMultiplier));
                    startLocation = livingEntity.getEyeLocation();
                    remainingDistance -= rayTraceResult.getHitPosition().distance(startLocation.toVector());
                }
            }
        }
    }

    @Override
    public double getDistance() {
        return distance;
    }

    @Override
    public void setDistance(double distance) {
        this.distance = distance;
    }

    @Override
    public double getPhysicalDamage() {
        return damage;
    }

    @Override
    public void setPhysicalDamage(double physicalDamage) {
        this.damage = physicalDamage;
    }

    @Override
    public double getPhysicalDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setPhysicalDamagePerLevel(double physicalDamagePerLevel) {
        this.damagePerLevel = physicalDamagePerLevel;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            event.setCancelled(true);
        }
    }

    public class ShockwaveEffect extends BukkitRunnable {
        private final Player player;
        private final Location startLocation;
        private final Location beamLocation;
        private final Vector direction;

        public ShockwaveEffect(Player player, Location startLocation) {
            this.player = player;
            this.startLocation = startLocation;
            this.beamLocation = startLocation.clone().add(0, 1, 0);
            this.direction = player.getLocation().getDirection().normalize().multiply(BEAM_SPEED);
        }

        @Override
        public void run() {
            beamLocation.add(direction);
            if (beamLocation.distanceSquared(startLocation) >= (distance * distance)) {
                this.cancel();
                return;
            }

            Block block = beamLocation.getBlock();

            // Create a falling block at the block's location
            if (!block.getType().equals(Material.AIR)) {
                FallingBlock fallingBlock = player.getWorld().spawnFallingBlock(block.getLocation().add(0.5, 1, 0.5), block.getBlockData());
                fallingBlock.setVelocity(new Vector(0, 0.25, 0)); // Set velocity so the block moves upwards
                fallingBlock.setDropItem(false); // Prevent the block from dropping an item when it lands
            }

            // Spawn smoke particle
            player.getWorld().spawnParticle(Particle.REDSTONE, beamLocation,
                    15, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 3));
        }
    }

}
