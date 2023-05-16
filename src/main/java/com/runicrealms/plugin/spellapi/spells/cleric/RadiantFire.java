package com.runicrealms.plugin.spellapi.spells.cleric;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
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

public class RadiantFire extends Spell implements AttributeSpell, DurationSpell {
    private static final Map<UUID, RadiantFireTask> RADIANT_FIRE_MAP = new HashMap<>();
    private double baseValue;
    private double maxStacks;
    private double multiplier;
    private double stackDuration;
    private String statName;

    public RadiantFire() {
        super("Radiant Fire", CharacterClass.CLERIC);
        this.setIsPassive(true);
        this.setDescription("Each time you land your &aSear &7spell, you gain " +
                "a stack (and refresh current stacks) of Radiant Fire! For each stack, you gain " + (multiplier * 100) +
                "% of your total &eWisdom✸ &7as increased healing! " +
                "While at max stacks, you glow bright with divine power " +
                "and your &aRadiant Nova &7has no warmup!" +
                "\nMax stacks: " + (int) maxStacks + "\nStacks expiry: " + stackDuration + "s");
    }

    /**
     * If a player has the passive, attempts to add a stack of radiant fire when 'Sear' is cast.
     * Fails if the player is >= max stacks
     *
     * @param event the spell cast event
     */
    private void attemptToStackRadiantFire(SpellCastEvent event) {
        if (!RADIANT_FIRE_MAP.containsKey(event.getCaster().getUniqueId())) {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                RADIANT_FIRE_MAP.remove(event.getCaster().getUniqueId());
                event.getCaster().setGlowing(false);
                event.getCaster().sendMessage(ChatColor.GRAY + "Radiant Fire has expired.");
            }, (int) stackDuration * 20L);
            RADIANT_FIRE_MAP.put(event.getCaster().getUniqueId(), new RadiantFireTask(new AtomicInteger(1), bukkitTask));
        } else {
            AtomicInteger stacks = RADIANT_FIRE_MAP.get(event.getCaster().getUniqueId()).getStacks();

            // Refresh stack duration
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(), () -> {
                RADIANT_FIRE_MAP.remove(event.getCaster().getUniqueId());
                event.getCaster().setGlowing(false);
                event.getCaster().sendMessage(ChatColor.GRAY + "Radiant Fire has expired.");
            }, (int) stackDuration * 20L);
            RADIANT_FIRE_MAP.get(event.getCaster().getUniqueId()).getBukkitTask().cancel();
            RADIANT_FIRE_MAP.get(event.getCaster().getUniqueId()).setBukkitTask(bukkitTask);

            if (stacks.get() >= maxStacks) return;
            // Increment stacks (add glow if max stacks reached)
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 0.5f);
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.5f);
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1);
            RADIANT_FIRE_MAP.get(event.getCaster().getUniqueId()).getAndIncrement();
            stacks = RADIANT_FIRE_MAP.get(event.getCaster().getUniqueId()).getStacks();
            if (stacks.get() >= maxStacks) {
                event.getCaster().setGlowing(true);
            }
        }
        // Send message feedback
        event.getCaster().sendMessage(ChatColor.GRAY + "Radiant Fire stacks: " + ChatColor.YELLOW + RADIANT_FIRE_MAP.get(event.getCaster().getUniqueId()).getStacks().get());
    }

    @Override
    public double getBaseValue() {
        return baseValue;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseValue = baseValue;
    }

    @Override
    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }

    @Override
    public double getDuration() {
        return stackDuration;
    }

    @Override
    public void setDuration(double duration) {
        this.stackDuration = duration;
    }

    @Override
    public void loadDurationData(Map<String, Object> spellData) {
        Number duration = (Number) spellData.getOrDefault("stack-duration", 0);
        setDuration(duration.doubleValue());
        Number stacks = (Number) spellData.getOrDefault("max-stacks", 0);
        setMaxStacks(stacks.doubleValue());
    }

    public double getMaxStacks() {
        return maxStacks;
    }

    public void setMaxStacks(double maxStacks) {
        this.maxStacks = maxStacks;
    }

    public Map<UUID, RadiantFireTask> getRadiantFireMap() {
        return RADIANT_FIRE_MAP;
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (!(event.getSpell() instanceof Sear)) return;
        attemptToStackRadiantFire(event);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getPlayer().getUniqueId(), this.getName())) return;
        if (event.getSpell() == null) return;
        if (!(event.getSpell() instanceof HealingSpell)) return;
        if (!RADIANT_FIRE_MAP.containsKey(event.getPlayer().getUniqueId())) return;
        int wisdom = RunicCore.getStatAPI().getPlayerWisdom(event.getPlayer().getUniqueId());
        double bonus = (multiplier * wisdom) / 100;
        bonus *= RADIANT_FIRE_MAP.get(event.getPlayer().getUniqueId()).getStacks().get();
        event.setAmount((int) (event.getAmount() + (event.getAmount() * bonus)));
    }

    /**
     * Used to keep track of the Radiant Fire stack refresh task.
     * Uses AtomicInteger to be thread-safe
     */
    static class RadiantFireTask {
        private final AtomicInteger stacks;
        private BukkitTask bukkitTask;

        public RadiantFireTask(AtomicInteger stacks, BukkitTask bukkitTask) {
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

