package com.runicrealms.plugin.events;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public RangedDamageEvent(int amount, @NotNull Player damager, @NotNull LivingEntity victim, boolean isBasicAttack, @NotNull Arrow arrow, @Nullable Spell spell) {
        super(amount, damager, victim, isBasicAttack, true, spell);
        this.arrow = arrow;
    }

    public RangedDamageEvent(int amount, @NotNull Player damager, @NotNull LivingEntity victim, boolean isBasicAttack, @NotNull Arrow arrow) {
        this(amount, damager, victim, isBasicAttack, arrow, null);
    }

    @NotNull
    public Arrow getArrow() {
        return arrow;
    }
}
