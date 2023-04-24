package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class RangedDamageEvent extends PhysicalDamageEvent {

    private final Arrow arrow;

    /**
     * Constructor of damage event w/ all the info we need!
     *
     * @param amount        of damage to inflict
     * @param damager       player who is causing damage
     * @param victim        who is receiving damage
     * @param isBasicAttack whether the attack is a physical spell or a basic attack
     * @param spell         optional parameter to specify a spell source (for damage scaling)
     */
    public RangedDamageEvent(int amount, Player damager, LivingEntity victim, boolean isBasicAttack, Arrow arrow, Spell... spell) {
        super(amount, damager, victim, isBasicAttack, true, spell);
        this.arrow = arrow;
    }

    public Arrow getArrow() {
        return arrow;
    }
}
