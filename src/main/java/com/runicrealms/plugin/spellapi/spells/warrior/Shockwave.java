package com.runicrealms.plugin.spellapi.spells.warrior;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.DistanceSpell;
import com.runicrealms.plugin.spellapi.spelltypes.PhysicalDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
public class Shockwave extends Spell implements DistanceSpell, PhysicalDamageSpell {
    private static final double BEAM_SPEED = 1.6; // 0.8
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

    private boolean checkForEnemies(Player caster, Location beamLocation) {
        for (Entity en : caster.getWorld().getNearbyEntities(beamLocation, BEAM_WIDTH, BEAM_WIDTH, BEAM_WIDTH)) {
            if (!isValidEnemy(caster, en)) continue;
            caster.getWorld().playSound(en.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1.0f, 1.0f);
            DamageUtil.damageEntityPhysical(damage, (LivingEntity) en, caster, false, false, this);
            en.setVelocity(new Vector(0, 1, 0).normalize().multiply(knockupMultiplier));
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
                if (finalBeamLocation.distanceSquared(fixed) >= (distance * distance))
                    this.cancel();
                player.getWorld().spawnParticle(Particle.REDSTONE, finalBeamLocation,
                        15, 0, 0, 0, 0, new Particle.DustOptions(Color.fromRGB(210, 180, 140), 3));
                checkForEnemies(player, finalBeamLocation);
            }
        }.runTaskTimer(plugin, 0, 5L);

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
}
