package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.events.EnvironmentDamage;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

/**
 * Converts vanilla MC damage to Runic Damage
 */
public class DamageEventUtil {
    private static final double CONTACT_DAMAGE_MULTIPLIER = 0.1;
    private static final double ENVIRONMENT_DAMAGE_MULTIPLIER = 0.15;
    private static final double ENVIRONMENT_KNOCKBACK_MULTIPLIER = -0.1;

    /**
     * This method calculates the damage to deal to a player based on the damage they would have taken
     * in vanilla Minecraft, plus some calculations.
     *
     * @param player      to deal damage to
     * @param eventDamage the vanilla damage
     * @return the runic damage to apply
     */
    public static int calculateRunicDamageFromVanillaDamage(Player player, double eventDamage, EnvironmentDamage.DamageCauses damageCause) {
        double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double multiplier = damageCause == EnvironmentDamage.DamageCauses.CONTACT ? getContactDamageMultiplier() : getEnvironmentDamageMultiplier();
        double percentDamage = ((eventDamage / 2) / 10) * multiplier; // First divide by 2 to account for half-hearts
        return (int) (maxHealth * percentDamage);
    }

    /**
     * Returns the multiplier for contact-based damage. This handles cacti, mostly
     *
     * @return the damage multiplier
     */
    private static double getContactDamageMultiplier() {
        return CONTACT_DAMAGE_MULTIPLIER;
    }

    /**
     * Returns the multiplier for environment-based damage. We do this to reduce impact of fall damage, etc.
     *
     * @return the damage multiplier
     */
    private static double getEnvironmentDamageMultiplier() {
        return ENVIRONMENT_DAMAGE_MULTIPLIER;
    }

    /**
     * Returns the knockback multiplier to apply after receiving environment damage
     *
     * @return the knockback multiplier
     */
    public static double getEnvironmentKnockbackMultiplier() {
        return ENVIRONMENT_KNOCKBACK_MULTIPLIER;
    }
}
