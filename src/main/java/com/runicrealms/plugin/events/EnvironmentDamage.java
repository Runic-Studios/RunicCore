package com.runicrealms.plugin.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * This custom event is called for damage sources OTHER than entities (lava, fall, etc.)
 */
public class EnvironmentDamage extends RunicDamageEvent {
    private final DamageCauses cause;

    public EnvironmentDamage(LivingEntity victim, int amount, DamageCauses cause) {
        super(victim, amount);
        this.cause = cause;
    }

    /**
     * A method that returns the possible cause of the damage
     *
     * @return the possible cause of the damage
     */
    public DamageCauses getCause() {
        return this.cause;
    }

    /**
     * A wrapper enum for all causes of generic damage on runic
     */
    public enum DamageCauses {

        CONTACT(EntityDamageEvent.DamageCause.CONTACT),
        DROWNING(EntityDamageEvent.DamageCause.DROWNING),
        FALL_DAMAGE(EntityDamageEvent.DamageCause.FALL),
        FIRE(EntityDamageEvent.DamageCause.FIRE),
        LAVA(EntityDamageEvent.DamageCause.LAVA),
        MAGMA_BLOCK(EntityDamageEvent.DamageCause.HOT_FLOOR),
        STARVATION(EntityDamageEvent.DamageCause.STARVATION);

        private final EntityDamageEvent.DamageCause cause;

        DamageCauses(EntityDamageEvent.DamageCause cause) {
            this.cause = cause;
        }

        public static DamageCauses getFromDamageCause(EntityDamageEvent.DamageCause cause) {
            for (DamageCauses causes : DamageCauses.values()) {
                if (causes.getCause().equals(cause)) {
                    return causes;
                }
            }

            return null;
        }

        private EntityDamageEvent.DamageCause getCause() {
            return this.cause;
        }
    }
}
