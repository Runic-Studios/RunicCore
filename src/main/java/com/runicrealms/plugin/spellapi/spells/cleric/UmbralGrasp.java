package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.spellapi.spelltypes.MagicDamageSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.spellapi.spelltypes.SpellItemType;
import com.runicrealms.plugin.spellapi.spellutil.particles.HorizontalCircleFrame;
import com.runicrealms.plugin.utilities.DamageUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class UmbralGrasp extends Spell implements MagicDamageSpell {
    private static final int DAMAGE_AMOUNT = 20;
    private static final int DAMAGE_PER_LEVEL = 1;
    private static final int DURATION = 3;
    private static final int MAX_DIST = 4;
    private static final int RADIUS = 1;
    private static final double BEAM_WIDTH = 1.5;
    private static final double PERIOD = 0.5;
    private final Map<UUID, Set<UUID>> damageMap = new HashMap<>();

    public UmbralGrasp() {
        super("Umbral Grasp",
                "You summon a wave of darkness at your target enemy or location within " + MAX_DIST + " " +
                        "blocks that travels backward. Enemies hit by the wave suffer (" + DAMAGE_AMOUNT + " + &f" + DAMAGE_PER_LEVEL
                        + "x&7 lvl) magicʔ damage, are pulled towards you, and are slowed for " + DURATION + "s!",
                ChatColor.WHITE, CharacterClass.CLERIC, 10, 25);
    }

    private void beginSpell(Player player, Location location, Location castLocation) {
        grasp(player, location);
        new BukkitRunnable() {
            double count = 1;

            @Override
            public void run() {
                if (count > MAX_DIST) {
                    this.cancel();
                    damageMap.remove(player.getUniqueId());
                } else {
                    count += 1 * PERIOD;
                    location.subtract(castLocation.getDirection());
                    grasp(player, location);
                }
            }
        }.runTaskTimer(RunicCore.getInstance(), 0, (long) PERIOD * 20L);
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
        if (rayTraceResult == null) {
            Location location = player.getTargetBlock(null, MAX_DIST).getLocation();
            beginSpell(player, location.setDirection(player.getEyeLocation().getDirection()),
                    player.getEyeLocation());
        } else if (rayTraceResult.getHitEntity() != null) {
            LivingEntity livingEntity = (LivingEntity) rayTraceResult.getHitEntity();
            beginSpell(player,
                    livingEntity.getEyeLocation().setDirection(player.getEyeLocation().getDirection()),
                    player.getEyeLocation());
        }
    }

    @Override
    public double getDamagePerLevel() {
        return DAMAGE_PER_LEVEL;
    }

    private void grasp(Player player, Location location) {
        if (!damageMap.containsKey(player.getUniqueId()))
            damageMap.put(player.getUniqueId(), new HashSet<>());
        new HorizontalCircleFrame(RADIUS, true).playParticle(player, Particle.ASH, location);
        player.getWorld().playSound(location, Sound.BLOCK_GLASS_BREAK, 0.25f, 0.25f);
        for (Entity entity : player.getWorld().getNearbyEntities(location, RADIUS, RADIUS, RADIUS, target -> isValidEnemy(player, target))) {
            if (damageMap.get(player.getUniqueId()).contains(entity.getUniqueId())) continue;
            DamageUtil.damageEntitySpell(DAMAGE_AMOUNT, (LivingEntity) entity, player, this);
            pullTarget(((LivingEntity) entity), player.getLocation(), entity.getLocation());
            ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, DURATION * 20, 2));
            damageMap.get(player.getUniqueId()).add(entity.getUniqueId());
        }
    }

    /**
     * @param target         who was hit by the spell
     * @param casterLocation location of the caster
     * @param targetLocation location of the target
     */
    private void pullTarget(LivingEntity target, Location casterLocation, Location targetLocation) {
        org.bukkit.util.Vector pushUpVector = new org.bukkit.util.Vector(0.0D, 0.4D, 0.0D);
        target.setVelocity(pushUpVector);
        final double xDir = (casterLocation.getX() - targetLocation.getX()) / 3.0D;
        double zDir = (casterLocation.getZ() - targetLocation.getZ()) / 3.0D;
        org.bukkit.util.Vector pushVector = new Vector(xDir, 0.0D, zDir).normalize().multiply(2).setY(0.4D);
        target.setVelocity(pushVector);
    }


}

