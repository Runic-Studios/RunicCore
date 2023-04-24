package com.runicrealms.plugin.spellapi.spellutil;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KnockbackUtil {
    private static final double MELEE_STRENGTH = 0.65;
    private static final double RANGED_STRENGTH = 0.5;

    /**
     * Controls strength of on-hit knockback for melee attacks against other players.
     *
     * @param damager Who hit the player (can be mob or another player)
     * @param victim  player who was hit
     */
    public static void knockbackMeleePlayer(Entity damager, Player victim) {
        // Calculate knockback direction
        Vector attackerPos = damager.getLocation().toVector();
        Vector enemyPos = victim.getLocation().toVector();
        Vector knockbackDirection = enemyPos.subtract(attackerPos).normalize();

        // Apply knockback to enemy
        Vector knockbackVector = knockbackDirection.multiply(MELEE_STRENGTH);
        knockbackVector.setY(knockbackVector.getY() + 0.15);
        victim.setVelocity(victim.getVelocity().add(knockbackVector));
    }

    /**
     * Controls strength of on-hit knockback for melee attacks against other players.
     *
     * @param damager player who attacked
     * @param victim  player who was hit
     */
    public static void knockbackRangedPlayer(Player damager, Player victim) {
        // Calculate knockback direction
        Vector attackerPos = damager.getLocation().toVector();
        Vector enemyPos = victim.getLocation().toVector();
        Vector knockbackDirection = enemyPos.subtract(attackerPos).normalize();

        // Apply knockback to enemy
        Vector knockbackVector = knockbackDirection.multiply(RANGED_STRENGTH);
        knockbackVector.setY(knockbackVector.getY() + 0.15);
        victim.setVelocity(victim.getVelocity().add(knockbackVector));
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
        // Calculate knockback direction
        Vector attackerPos = damager.getLocation().toVector();
        Vector enemyPos = entity.getLocation().toVector();
        Vector knockbackDirection = enemyPos.subtract(attackerPos).normalize();

        // Apply knockback to enemy
        double knockbackStrength = isRanged ? RANGED_STRENGTH : MELEE_STRENGTH;
        Vector knockbackVector = knockbackDirection.multiply(knockbackStrength);
        knockbackVector.setY(knockbackVector.getY() + 0.15);
        entity.setVelocity(entity.getVelocity().add(knockbackVector));
    }
}

