package com.runicrealms.plugin.spellapi.effect.cleric;

import com.runicrealms.plugin.spellapi.effect.SpellEffectType;
import com.runicrealms.plugin.spellapi.effect.StackEffect;
import com.runicrealms.plugin.spellapi.effect.StackHologram;
import com.runicrealms.plugin.spellapi.modeled.ModeledSpellAttached;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class RadiantFireEffect implements StackEffect {
    private static final String MODEL_ID = "radiant_fire";
    private static final int PERIOD = 20;
    private final Player caster;
    private final int maxStacks;
    private final int stackThreshold;
    private final int stackDuration;
    private final AtomicInteger stacks;
    private final StackHologram stackHologram;
    private Location hologramLocation;
    private int nextTickCounter;
    private ModeledSpellAttached modeledSpellAttached;

    /**
     * @param caster           uuid of the caster
     * @param maxStacks        max stacks caster can earn
     * @param stackThreshold   stacks required for 'buffed' state
     * @param stackDuration    how long before each stack falls off
     * @param initialStacks    how many stacks to start with
     * @param hologramLocation initial location to spawn the hologram
     */
    public RadiantFireEffect(Player caster, int maxStacks, int stackThreshold, int stackDuration, int initialStacks, Location hologramLocation) {
        this.caster = caster;
        this.maxStacks = maxStacks;
        this.stackThreshold = stackThreshold;
        this.stackDuration = stackDuration;
        this.stacks = new AtomicInteger(initialStacks);
        this.hologramLocation = hologramLocation;
        this.stackHologram = new StackHologram(
                SpellEffectType.RADIANT_FIRE,
                hologramLocation,
                Set.of(caster)
        );
    }

    public int getMaxStacks() {
        return maxStacks;
    }

    @Override
    public void setNextTickCounter(int nextTickCounter) {
        this.nextTickCounter = nextTickCounter;
    }

    public void setHologramLocation(Location hologramLocation) {
        this.hologramLocation = hologramLocation;
    }

    @Override
    public AtomicInteger getStacks() {
        return stacks;
    }

    public void increment(Location hologramLocation, int amountToIncrement) {
        this.setHologramLocation(hologramLocation.add(0, 1.5f, 0));
        int currentStacks = this.stacks.get();
        if (currentStacks >= this.maxStacks) {
            return;
        }
        this.stacks.set(Math.min(currentStacks + amountToIncrement, this.maxStacks));
        executeSpellEffect();
    }

    @Override
    public SpellEffectType getEffectType() {
        return SpellEffectType.RADIANT_FIRE;
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
        if (globalCounter % 60 == 0) { // Show particle effect / hologram every 60 ticks
            executeSpellEffect();
        }
        if (globalCounter < nextTickCounter) {
            return;
        }
        if (caster.isDead()) {
            cancel();
            return;
        }
        // Decrement one stack every stackDuration seconds
        if (stacks.get() > 0) {
            stacks.getAndDecrement();
            caster.playSound(caster.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.25f, 3.0f);
            executeSpellEffect();
        }
        // Set the next tick
        nextTickCounter += getTickInterval();
    }

    @Override
    public void executeSpellEffect() {
        int stacks = this.stacks.get();
        stackHologram.showHologram(this.hologramLocation, this.stacks.get());
        if (stacks >= stackThreshold && this.modeledSpellAttached == null) {
            this.modeledSpellAttached = spawnModel();
        } else if (stacks < stackThreshold && this.modeledSpellAttached != null) {
            modeledSpellAttached.cancel();
            this.modeledSpellAttached = null;
        }
    }

    @Override
    public void cancel() {
        stacks.set(0);
        caster.playSound(caster.getLocation(), Sound.BLOCK_CONDUIT_DEACTIVATE, 0.25f, 3.0f);
        if (this.modeledSpellAttached != null) {
            modeledSpellAttached.cancel();
        }
    }

    @Override
    public int getTickInterval() {
        return PERIOD * stackDuration;
    }

    @Override
    public StackHologram getStackHologram() {
        return stackHologram;
    }

    public int getStackThreshold() {
        return stackThreshold;
    }

    private ModeledSpellAttached spawnModel() {
        ModeledSpellAttached modeledSpellAttached = new ModeledSpellAttached(
                caster,
                MODEL_ID,
                this.caster.getLocation(),
                1.0,
                999,
                target -> false
        );
        modeledSpellAttached.initialize();
        modeledSpellAttached.getModeledEntity().getModels().forEach((s, activeModel) -> activeModel.getAnimationHandler().playAnimation(
                "idle",
                0.5,
                0.5,
                1.0,
                false
        ));
        return modeledSpellAttached;
    }
}
