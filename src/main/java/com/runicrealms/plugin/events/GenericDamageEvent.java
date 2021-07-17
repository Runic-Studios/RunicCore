package com.runicrealms.plugin.events;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

public class GenericDamageEvent extends RunicDamageEvent {
    private final DamageCauses cause;

    public GenericDamageEvent(LivingEntity victim, int amount, DamageCauses cause) {
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
    //feel free to add more if we need more (I built a wrapper so things like entity damage cant be thrown in)
    public enum DamageCauses {
        LAVA(EntityDamageEvent.DamageCause.LAVA),
        CONTACT(EntityDamageEvent.DamageCause.CONTACT),
        MAGMA_BLOCK(EntityDamageEvent.DamageCause.HOT_FLOOR),
        FALL_DAMAGE(EntityDamageEvent.DamageCause.FALL);

        private final EntityDamageEvent.DamageCause cause;

        DamageCauses(EntityDamageEvent.DamageCause cause) {
            this.cause = cause;
        }

        private EntityDamageEvent.DamageCause getCause() {
            return this.cause;
        }

        public static DamageCauses getFromDamageCause(EntityDamageEvent.DamageCause cause) {
            for (DamageCauses causes : DamageCauses.values()) {
                if (causes.getCause().equals(cause)) {
                    return causes;
                }
            }

            return null;
        }
    }
}
