package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Used to keep track of abilities which have 'stacks', like Charged or Soul Reaper
 * Uses AtomicInteger to be thread-safe
 * Maintains a reference to a BukkitTask to remove all stacks, which can be cancelled and refreshed
 */
public class StackTask {
    private static final StackTaskRegistry STACK_TASK_REGISTRY = new StackTaskRegistry();

    private final Player caster;
    private final Spell spell;
    private final AtomicInteger stacks;
    private BukkitTask bukkitTask;

    /**
     * @param caster     who is gaining/losing stacks
     * @param spell      responsible for the change
     * @param stacks     that the player currently has (thread-safe)
     * @param bukkitTask to eventually cancel this task
     */
    public StackTask(Player caster, Spell spell, AtomicInteger stacks, BukkitTask bukkitTask) {
        this.caster = caster;
        this.spell = spell;
        this.stacks = stacks;
        this.bukkitTask = bukkitTask;
        // Register this task so that we can cancel it if the player crashes, logs out, etc.
        STACK_TASK_REGISTRY.registerStackTask(this);
    }

    public Spell getSpell() {
        return spell;
    }

    public Player getCaster() {
        return caster;
    }

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public AtomicInteger getStacks() {
        return stacks;
    }

    public void reset(long duration, Runnable cleanupTask) {
        this.bukkitTask.cancel();
        this.bukkitTask = Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(),
                cleanupTask, duration * 20L);
    }

}
