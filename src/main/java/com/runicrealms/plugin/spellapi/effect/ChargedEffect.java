package com.runicrealms.plugin.spellapi.effect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.concurrent.atomic.AtomicInteger;

public class ChargedEffect implements SpellEffect {
    private static final int PERIOD = 20;
    private final Player caster;
    private final int maxStacks;
    private final int stackDuration;
    private final AtomicInteger stacks;

    /**
     * @param caster        uuid of the caster
     * @param maxStacks     max stacks caster can earn
     * @param stackDuration how long before each stack falls off
     */
    public ChargedEffect(Player caster, int maxStacks, int stackDuration) {
        this.caster = caster;
        this.maxStacks = maxStacks;
        this.stackDuration = stackDuration;
        this.stacks = new AtomicInteger(1);
    }

    public AtomicInteger getStacks() {
        return stacks;
    }

    public void increment() {
        if (this.stacks.get() >= this.maxStacks) {
            return;
        }
        this.stacks.getAndIncrement();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.CHARGED;
    }

    @Override
    public boolean isActive() {
        return stacks.get() > 0;
    }

    @Override
    public boolean isBuff() {
        return true;
    }

    @Override
    public Player getCaster() {
        return caster;
    }

    @Override
    public LivingEntity getRecipient() {
        return caster;
    }

    @Override
    public void tick(int counter) {
        if (caster.isDead()) {
            stacks.set(0);
            return;
        }
        // Decrement one stack every few seconds
        if (counter % (PERIOD * stackDuration) == 0) {
            if (stacks.get() > 0) {
                stacks.getAndDecrement();
                // todo: recalculate additional attk speed here
            }
        }
        // todo: cone if at max stacks
    }

    @Override
    public int getTickInterval() {
        return PERIOD;
    }
}
