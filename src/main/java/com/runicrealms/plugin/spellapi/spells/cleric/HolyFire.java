package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.HealingSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class HolyFire extends Spell {
    private static final int MAX_STACKS = 5;
    private static final int STACK_DURATION = 12;
    private static final double PERCENT = .10;
    private final Map<UUID, HolyFireTask> holyFireMap = new HashMap<>();

    public HolyFire() {
        super("Holy Fire",
                "Each time you land your &aSear &7spell, you gain " +
                        "a stack of Holy Fire! For each stack, you gain " + (int) (PERCENT * 100) +
                        "% of your total &eWisdom✸ &7as increased healing! " +
                        "Each &aSear &7refreshes the duration " +
                        "of your stacks. While at max stacks, " +
                        "you glow bright with holy power!" +
                        "\nMax stacks: " + MAX_STACKS + "\nStacks expiry: " + STACK_DURATION + "s",
                ChatColor.WHITE, CharacterClass.CLERIC, 0, 0);
        this.setIsPassive(true);
    }

    /**
     * If a player has the passive, attempts to add a stack of holy fire when 'Sear' is cast.
     * Fails if the player is >= max stacks
     *
     * @param event the spell cast event
     */
    private void attemptToStackHolyFire(SpellCastEvent event) {
        if (!holyFireMap.containsKey(event.getCaster().getUniqueId())) {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                holyFireMap.remove(event.getCaster().getUniqueId());
                event.getCaster().setGlowing(false);
            }, STACK_DURATION * 20L);
            holyFireMap.put(event.getCaster().getUniqueId(), new HolyFireTask(new AtomicInteger(1), bukkitTask));
        } else {
            AtomicInteger stacks = holyFireMap.get(event.getCaster().getUniqueId()).getStacks();

            // Refresh stack duration
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                holyFireMap.remove(event.getCaster().getUniqueId());
                event.getCaster().setGlowing(false);
            }, STACK_DURATION * 20L);
            holyFireMap.get(event.getCaster().getUniqueId()).getBukkitTask().cancel();
            holyFireMap.get(event.getCaster().getUniqueId()).setBukkitTask(bukkitTask);

            if (stacks.get() >= MAX_STACKS) return;
            // Increment stacks (add glow if max stacks reached)
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 0.5f);
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.5f);
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1);
            holyFireMap.get(event.getCaster().getUniqueId()).getAndIncrement();
            stacks = holyFireMap.get(event.getCaster().getUniqueId()).getStacks();
            if (stacks.get() >= MAX_STACKS) {
                event.getCaster().setGlowing(true);
            }
        }
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Sear)) return;
        attemptToStackHolyFire(event);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof HealingSpell)) return;
        if (!holyFireMap.containsKey(event.getPlayer().getUniqueId())) return;
        int wisdom = RunicCore.getStatAPI().getPlayerWisdom(event.getPlayer().getUniqueId());
        double bonus = (PERCENT * wisdom) / 100;
        bonus *= holyFireMap.get(event.getPlayer().getUniqueId()).getStacks().get();
        event.setAmount((int) (event.getAmount() + (event.getAmount() * bonus)));
    }

    /**
     * Used to keep track of the Holy Fire stack refresh task.
     * Uses AtomicInteger to be thread-safe
     */
    static class HolyFireTask {
        private final AtomicInteger stacks;
        private BukkitTask bukkitTask;

        public HolyFireTask(AtomicInteger stacks, BukkitTask bukkitTask) {
            this.stacks = stacks;
            this.bukkitTask = bukkitTask;
        }

        public void getAndIncrement() {
            this.stacks.getAndIncrement();
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
    }
}

