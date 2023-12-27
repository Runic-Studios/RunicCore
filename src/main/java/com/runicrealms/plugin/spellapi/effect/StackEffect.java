package com.runicrealms.plugin.spellapi.effect;

public interface StackEffect extends SpellEffect {

    /**
     * Initializes the next tick counter for the effect.
     * Used to solve a problem where the first tick of a spell effect
     * may execute too quickly based on the state of the globalCounter.
     * This way, the first tick of the effect is always on time
     *
     * @param globalCounter The current value of the global counter.
     */
    default void initializeNextTick(int globalCounter) {
        int nextTickCounter = globalCounter + getTickInterval();
        setNextTickCounter(nextTickCounter);
    }

    /**
     * Sets the next tick counter value.
     *
     * @param nextTickCounter The value of the next tick counter.
     */
    void setNextTickCounter(int nextTickCounter);

    /**
     * @return the interval at which this specific effect should tick (in game ticks! e.g. 40 = 2 seconds)
     */
    int getTickInterval();

    /**
     * A hologram displayed only to the relevant parties to show the current number of stacks
     *
     * @return a client-side StackHologram
     */
    StackHologram getStackHologram();
}
