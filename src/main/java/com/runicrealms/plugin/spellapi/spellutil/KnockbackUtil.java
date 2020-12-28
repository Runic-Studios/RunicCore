package com.runicrealms.plugin.spellapi.spellutil;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class KnockbackUtil {

    private static final double POWER = 0.35;

    // todo: fix this
    /**
     * Controls strength of on-hit knockback for melee attacks against other players.
     * @param damager Who hit the player (can be mob or another player)
     * @param victim player who was hit
     */
    public static void knockbackMeleePlayer(Entity damager, Player victim) {
        Location l = victim.getLocation().subtract(damager.getLocation());
        if (victim.getLocation().getWorld() != damager.getLocation().getWorld()) return;
        double distance = victim.getLocation().distance(damager.getLocation());
        if (distance == 0) distance = 1;
        DecimalFormat finite = new DecimalFormat("#.####");
        double multiplier = POWER/distance;
        Vector vec = l.toVector().normalize().multiply(multiplier);
        vec.setX(Double.parseDouble(finite.format(vec.getX())));
        vec.setY(0.3333);
        vec.setZ(Double.parseDouble(finite.format(vec.getZ())));
        victim.setVelocity(vec);
    }

    /**
     * Controls strength of on-hit knockback for melee attacks against other players.
     * @param damager player who attacked
     * @param victim player who was hit
     */
    public static void knockbackRangedPlayer(Player damager, Player victim) {
        Vector vector = damager.getLocation().getDirection().multiply(0.35);
        vector.setY(0.2);
        victim.setVelocity(vector);
    }

    /**
     * Controls strength of on-hit knockback for melee attacks against mobs.
     * @param entity entity to be hit back
     * @param isRanged whether the attack came from a melee or ranged hit
     */
    public static void knockBackMob(Player damager, Entity entity, boolean isRanged) {
        double multiplier = 0.5;
        if (isRanged)
            multiplier = 0.4;
        Vector vector = damager.getLocation().getDirection().multiply(multiplier);
        vector.setY(0.225);
        // no boss knockback!
        if (MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).isPresent()) {
            ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).get();
            if (am.hasFaction() && am.getFaction().equalsIgnoreCase("boss")) {
                return;
            }
        }
        entity.setVelocity(vector);
    }
}

