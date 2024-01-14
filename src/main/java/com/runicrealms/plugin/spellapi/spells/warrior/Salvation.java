package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.RadiusSpell;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.VectorUtil;
import com.runicrealms.plugin.utilities.BlocksUtil;
import com.runicrealms.plugin.utilities.DamageUtil;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("FieldCanBeLocal")
public class Salvation extends Spell implements DistanceSpell, DurationSpell, MagicDamageSpell, RadiusSpell, ShieldingSpell {
    private final Map<Block, BellTask> blockMap = new HashMap<>();
    private double damage;
    private double damagePerLevel;
    private double distance;
    private double duration;
    private double maxDistance;
    private double radius;
    private double shield;
    private double shieldPerLevel;

    public Salvation() {
        super("Salvation", CharacterClass.WARRIOR);
        this.setDescription("You conjure a bell up to " + distance + " blocks away! " +
                "For the next " + duration + "s, the bell charges with &6holy power&7! " +
                "You or an ally can right-click the bell; allies will be teleported to you! " +
                "Upon clicking the bell, it explodes, granting you or your ally a &eshield &7equal to (" + shield + "x &6holy power &7+ &f" +
                shieldPerLevel + "x&7 lvl) and dealing (" + damage + "x &6holy power &7+ &f" + damagePerLevel
                + "x&7 lvl) magicʔ damage to enemies within " + radius + " blocks!" +
                "\n\n&2&lEFFECT &eShield" +
                "\n&7Shields absorb damage and appear as yellow hearts!");
    }

    private static String determineHologramString(int count) {
        return switch (count) {
            case 2 -> ChatColor.GOLD + "★★" + ChatColor.WHITE + "☆☆";
            case 3 -> ChatColor.GOLD + "★★★" + ChatColor.WHITE + "☆";
            case 4 -> ChatColor.GOLD + "★★★★";
            default -> ChatColor.GOLD + "★" + ChatColor.WHITE + "☆☆☆";
        };
    }

    @Override
    public void executeSpell(Player player, SpellItemType type) {
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 1.0f);
        Location location = player.getTargetBlock(null, (int) distance).getLocation();
        if (location.getBlock().getType() != Material.AIR)
            location.add(0, 1, 0); // Try to prevent bell spawning in floor
        VectorUtil.drawLine(player, Particle.VILLAGER_HAPPY, Color.WHITE, player.getEyeLocation(), location, 0.5D, 1, 0.25f);
        player.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, location, 8, 0.5f, 0.5f, 0.5f, 0);
        spawnBell(player, location);
    }

    @Override
    public void loadDistanceData(Map<String, Object> spellData) {
        Number distance = (Number) spellData.getOrDefault("distance", 0);
        setDistance(distance.doubleValue());
        Number maxDistance = (Number) spellData.getOrDefault("max-distance", 0);
        setMaxDistance(maxDistance.doubleValue());
    }

    public void setMaxDistance(double maxDistance) {
        this.maxDistance = maxDistance;
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
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = duration;
    }

    @EventHandler
    public void onBellRung(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        if (blockMap.isEmpty()) return;
        if (!(event.getHand() == EquipmentSlot.HAND)) return;
        if (event.getClickedBlock() == null) return;
        if (action != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock().getType() != Material.BELL) return;
        Optional<Block> optional = blockMap.keySet().stream().filter(block1 -> block1.equals(event.getClickedBlock())).findFirst();
        if (optional.isEmpty()) return;
        Block block = optional.get();
        Player caster = Bukkit.getPlayer(blockMap.get(block).getCasterUUID());
        if (caster == null) return;
        if (!isValidAlly(caster, player)) return; // Only allies can click the bell
        player.getWorld().playSound(event.getClickedBlock().getLocation(), Sound.BLOCK_BELL_USE, 0.5f, 2.0f);
        // Destroy bell
        int count = blockMap.get(block).getCount().get();
        blockMap.get(block).getBukkitTask().cancel();
        blockMap.get(block).cleanupTask();
        // Damage nearby entities
        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_DRAGON_FIREBALL_EXPLODE, 0.5f, 2.0f);
        block.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, block.getLocation(), 15, 2.0f, 2.0f, 2.0f, 0);
        for (Entity entity : block.getWorld().getNearbyEntities(block.getLocation(), radius, radius, radius, target -> isValidEnemy(caster, target))) {
            DamageUtil.damageEntitySpell(damage * count, (LivingEntity) entity, caster, this);
        }
        shieldPlayer(caster, player, shield * count, this);
        // An ally clicked the bell (NOT the caster)
        if (!player.getUniqueId().equals(blockMap.get(block).getCasterUUID())) {
            boolean canTeleport = false;
            if (player.getWorld() == caster.getWorld()) {
                double distanceSquared = player.getLocation().distanceSquared(caster.getLocation());
                if (distanceSquared <= maxDistance * maxDistance) canTeleport = true;
            }
            if (canTeleport) {
                player.teleport(caster);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
            } else {
                player.sendMessage(ChatColor.RED + "Could not teleport you to the caster! Target is too far away!");
                player.getWorld().playSound(player.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            }
        }
    }

    /**
     * Spawns a bell block with a task to remove it. Tries to find the 'best' location in a 3 block
     * radius
     *
     * @param location to spawn the block
     */
    private void spawnBell(Player caster, Location location) {
        Location bestLocation = BlocksUtil.findNearestAir(location, 3);
        if (bestLocation == null) { // Couldn't find a nearby air block
            caster.sendMessage(ChatColor.RED + "A valid location could not be found!");
            caster.playSound(caster.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            return;
        }
        Material oldMaterial = bestLocation.getBlock().getType();
        bestLocation.getBlock().setType(Material.BELL, false);
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(bestLocation.clone().add(0.5, 2.5, 0.5));
        hologram.getLines().appendText(ChatColor.WHITE + caster.getName() + "'s " + ChatColor.GRAY + "Bell");
        hologram.getLines().appendText(ChatColor.GREEN + String.valueOf(ChatColor.BOLD) + "CLICK ME!");
        hologram.getLines().appendText(determineHologramString(0));
        caster.getWorld().playSound(location, Sound.BLOCK_BELL_USE, 0.5f, 1.0f);
        BellTask bellTask = new BellTask(caster.getUniqueId(), hologram, bestLocation, oldMaterial, duration);
        blockMap.put(bestLocation.getBlock(), bellTask);
    }

    @Override
    public double getMagicDamage() {
        return damage;
    }

    @Override
    public void setMagicDamage(double magicDamage) {
        this.damage = magicDamage;
    }

    @Override
    public double getMagicDamagePerLevel() {
        return damagePerLevel;
    }

    @Override
    public void setMagicDamagePerLevel(double magicDamagePerLevel) {
        this.damagePerLevel = magicDamagePerLevel;
    }

    @Override
    public double getShield() {
        return shield;
    }

    @Override
    public void setShield(double shield) {
        this.shield = shield;
    }

    @Override
    public double getShieldingPerLevel() {
        return shieldPerLevel;
    }

    @Override
    public void setShieldPerLevel(double shieldingPerLevel) {
        this.shieldPerLevel = shieldingPerLevel;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * Used to keep track of the Radiant Fire stack refresh task.
     * Uses AtomicInteger to be thread-safe
     */
    static class BellTask {
        private final AtomicInteger count = new AtomicInteger(1);
        private final UUID casterUUID;
        private final Hologram hologram;
        private final Location bestLocation;
        private final Material oldMaterial;
        private final BukkitTask bukkitTask;

        public BellTask(UUID casterUUID, Hologram hologram, Location bestLocation, Material oldMaterial, double duration) {
            this.casterUUID = casterUUID;
            this.hologram = hologram;
            this.bestLocation = bestLocation;
            this.oldMaterial = oldMaterial;

            bukkitTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if (count.get() >= duration) {
                        this.cancel();
                        cleanupTask();
                    } else {
                        count.getAndIncrement();
                        hologram.getLines().remove(2);
                        hologram.getLines().appendText(determineHologramString(count.get()));
                    }
                }
            }.runTaskTimer(RunicCore.getInstance(), 20L, 20L);
        }

        public AtomicInteger getCount() {
            return count;
        }

        private void cleanupTask() {
            hologram.delete();
            bestLocation.getBlock().setType(oldMaterial);
        }

        public BukkitTask getBukkitTask() {
            return bukkitTask;
        }

        public UUID getCasterUUID() {
            return casterUUID;
        }
    }

}

