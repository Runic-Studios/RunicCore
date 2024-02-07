package com.runicrealms.plugin.spellapi.effect.warrior;

import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.StackEffect;
import com.runicrealms.plugin.spellapi.effect.StackHologram;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BlessedBladeEffect implements StackEffect {
    private static final int PERIOD = 20;
    private final Player caster;
    private final int maxStacks;
    private final int stackDuration;
    private final AtomicInteger stacks;
    private final StackHologram stackHologram;
    private Location hologramLocation;
    private int nextTickCounter;

    /**
     * @param caster           uuid of the caster
     * @param maxStacks        max stacks caster can earn
     * @param stackDuration    how long before each stack falls off
     * @param initialStacks    how many stacks to start with
     * @param hologramLocation initial location to spawn the hologram
     */
    public BlessedBladeEffect(Player caster, int maxStacks, int stackDuration, int initialStacks, Location hologramLocation) {
        this.caster = caster;
        this.maxStacks = maxStacks;
        this.stackDuration = stackDuration;
        this.stacks = new AtomicInteger(initialStacks);
        this.hologramLocation = hologramLocation;
        this.stackHologram = new StackHologram(
                SpellEffectType.BLESSED_BLADE,
                hologramLocation,
                Set.of(caster)
        );
        executeSpellEffect();
    }

    @Override
    public void setNextTickCounter(int nextTickCounter) {
        this.nextTickCounter = nextTickCounter;
    }

    public void refresh(Location hologramLocation, int globalCounter) {
        this.setHologramLocation(hologramLocation.add(0, 1.5f, 0));
        this.stacks.set(this.maxStacks);
        this.nextTickCounter = globalCounter + getTickInterval();
    }

    public void setHologramLocation(Location hologramLocation) {
        this.hologramLocation = hologramLocation;
    }

    @Override
    public AtomicInteger getStacks() {
        return stacks;
    }

    public void decrement(Location hologramLocation, int amountToDecrement) {
        this.setHologramLocation(hologramLocation.add(0, 1.5f, 0));
        int currentStacks = this.stacks.get();
        if (currentStacks == 0) {
            return;
        }
        this.stacks.set(Math.max(currentStacks - amountToDecrement, 0));
        executeSpellEffect();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.BLESSED_BLADE;
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
    public void tick(int globalCounter) {
        if (globalCounter < nextTickCounter) {
            return;
        }
        if (caster.isDead()) {
            cancel();
            return;
        }
        // Cancels stacks every stackDuration seconds
        if (stacks.get() > 0) {
            this.cancel();
        }
        executeSpellEffect();
        // Set the next tick
        nextTickCounter += getTickInterval();
    }

    @Override
    public void executeSpellEffect() {
        stackHologram.showHologram(this.hologramLocation, this.stacks.get());
    }

    @Override
    public void cancel() {
        stacks.set(0);
        caster.playSound(caster.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.25f, 3.0f);
    }

    @Override
    public int getTickInterval() {
        return PERIOD * stackDuration;
    }

    @Override
    public StackHologram getStackHologram() {
        return stackHologram;
    }
}
