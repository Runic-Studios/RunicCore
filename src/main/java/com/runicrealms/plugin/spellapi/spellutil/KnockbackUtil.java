package com.runicrealms.plugin.spellapi.spellutil;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KnockbackUtil {
    private static final double MELEE_STRENGTH = 0.65;
    private static final double MAX_VELOCITY = 0.65D;
    private static final double RANGED_STRENGTH = 0.5;
    private static final double VERTICAL_COMPONENT = 0.15;

    /**
     * Controls strength of on-hit knockback for melee attacks against other players.
     *
     * @param damager Who hit the player (can be mob or another player)
     * @param victim  player who was hit
     */
    public static void knockbackMeleePlayer(Entity damager, Player victim) {
        applyKnockback(damager, victim, MELEE_STRENGTH);
    }

    private static void applyKnockback(Entity damager, Entity victim, double multiplier) {
        // Calculate knockback direction
        Vector attackerPos = damager.getLocation().toVector();
        Vector enemyPos = victim.getLocation().toVector();
        Vector knockbackDirection = enemyPos.subtract(attackerPos);

        // Check if the distance between the entities is above a certain threshold
        double minDistance = 0.1; // Define a minimum distance threshold
        if (knockbackDirection.lengthSquared() < minDistance * minDistance) {
            return; // Do not apply knockback if the distance is too small
        }

        // Normalize the knockback direction
        knockbackDirection.normalize();

        // Apply knockback to enemy
        Vector knockbackVector = knockbackDirection.multiply(multiplier);
        knockbackVector.setY(VERTICAL_COMPONENT);
        Vector newVelocity = victim.getVelocity().add(knockbackVector);

        // Limit the victim's velocity magnitude
        double maxVelocity = MAX_VELOCITY;
        if (newVelocity.length() > maxVelocity) {
            newVelocity = newVelocity.normalize().multiply(maxVelocity);
        }

        // Limit the y-component of the velocity
        double maxYVelocity = 0.5D;  // Define your maximum y-velocity here
        if (newVelocity.getY() > maxYVelocity) {
            newVelocity.setY(maxYVelocity);
        }

        victim.setVelocity(newVelocity);
    }


    /**
     * Controls strength of on-hit knockback for ranged attacks against other players.
     *
     * @param damager player who attacked
     * @param victim  player who was hit
     */
    public static void knockbackRangedPlayer(Player damager, Player victim) {
        applyKnockback(damager, victim, RANGED_STRENGTH);
    }

    /**
     * Controls strength of on-hit knockback for melee attacks against mobs.
     *
     * @param entity   entity to be hit back
     * @param isRanged whether the attack came from a melee or ranged hit
     */
    public static void knockBackMob(Player damager, Entity entity, boolean isRanged) {
        // No boss knockback!
        if (MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).isPresent()) {
            ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(entity.getUniqueId()).get();
            if (am.hasFaction() && am.getFaction().equalsIgnoreCase("boss")) {
                return;
            }
        }
        double knockbackStrength = isRanged ? RANGED_STRENGTH : MELEE_STRENGTH;
        applyKnockback(damager, entity, knockbackStrength);
    }
}

