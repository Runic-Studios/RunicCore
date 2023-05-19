package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
import com.runicrealms.plugin.spellapi.spells.Potion;
import com.runicrealms.plugin.spellapi.spelltypes.AttributeSpell;
import com.runicrealms.plugin.spellapi.spelltypes.DurationSpell;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Charged extends Spell implements AttributeSpell, DurationSpell {
    private static final int MAX_STACKS = 5;
    private final Map<UUID, ChargedTask> chargedMap = new HashMap<>();
    private int duration = 6;
    private double intMultiplier = 0.1;
    private int baseInt = 2;
    private String statName = "";

    public Charged() {
        super("Charged", CharacterClass.ARCHER);
        this.setIsPassive(true);
        this.setDescription("Every time you cast a spell, gain " +
                "additional magicʔ damage equal to (" + baseInt + " + &f"
                + intMultiplier + "x &7lvl) of your &e" + getStatName() + "ʔ&7! " +
                "This effect can stack up to " + MAX_STACKS + " times. " +
                "After not casting a spell for " + duration + "s, remove all stacks. " +
                "When fully charged, you glow brightly!");
    }

    /**
     * If a player has the passive, attempts to add a stack of charged when a spell is cast
     * Fails if the player is >= max stacks
     *
     * @param event the spell cast event
     */
    private void attemptToStackCharged(SpellCastEvent event) {
        if (!chargedMap.containsKey(event.getCaster().getUniqueId())) {
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> cleanupTask(event.getCaster()), duration * 20L);
            chargedMap.put(event.getCaster().getUniqueId(), new ChargedTask(new AtomicInteger(1), bukkitTask));
        } else {
            AtomicInteger stacks = chargedMap.get(event.getCaster().getUniqueId()).getStacks();

            // Refresh stack duration
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> cleanupTask(event.getCaster()), duration * 20L);
            chargedMap.get(event.getCaster().getUniqueId()).getBukkitTask().cancel();
            chargedMap.get(event.getCaster().getUniqueId()).setBukkitTask(bukkitTask);

            if (stacks.get() >= MAX_STACKS) return;
            // Increment stacks (add glow if max stacks reached)
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 0.5f);
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FIRE_AMBIENT, 0.5f, 0.5f);
            event.getCaster().playSound(event.getCaster().getLocation(), Sound.BLOCK_FURNACE_FIRE_CRACKLE, 0.5f, 1);
            chargedMap.get(event.getCaster().getUniqueId()).getAndIncrement();
            stacks = chargedMap.get(event.getCaster().getUniqueId()).getStacks();
            if (stacks.get() >= MAX_STACKS) {
                event.getCaster().setGlowing(true);
            }
        }
        // Send message feedback
        event.getCaster().sendMessage(ChatColor.GRAY + "Charged stacks: " + ChatColor.YELLOW + chargedMap.get(event.getCaster().getUniqueId()).getStacks().get());
    }

    /**
     * @param player whose stacks have expired
     */
    private void cleanupTask(Player player) {
        chargedMap.remove(player.getUniqueId());
        player.setGlowing(false);
        player.sendMessage(ChatColor.GRAY + "Charged has expired.");
    }

    @Override
    public double getBaseValue() {
        return baseInt;
    }

    @Override
    public void setBaseValue(double baseValue) {
        this.baseInt = (int) baseValue;
    }

    @Override
    public double getMultiplier() {
        return intMultiplier;
    }

    @Override
    public void setMultiplier(double multiplier) {
        this.intMultiplier = multiplier;
    }

    @Override
    public String getStatName() {
        return this.statName;
    }

    @Override
    public void setStatName(String statName) {
        this.statName = statName;
    }

    @Override
    public double getDuration() {
        return duration;
    }

    @Override
    public void setDuration(double duration) {
        this.duration = (int) duration;
    }

    @EventHandler(priority = EventPriority.HIGHEST) // late
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!chargedMap.containsKey(event.getPlayer().getUniqueId())) return;
        UUID uuid = event.getPlayer().getUniqueId();
        int damageToGrant = (int) (baseInt + (RunicCore.getStatAPI().getStat(uuid, getStatName()) * intMultiplier));
        damageToGrant *= chargedMap.get(event.getPlayer().getUniqueId()).getStacks().get();
        event.setAmount(event.getAmount() + damageToGrant);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!event.willExecute()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
        if (event.getSpell() instanceof Potion) return;
        attemptToStackCharged(event);
    }

    /**
     * Used to keep track of the Charged stack refresh task.
     * Uses AtomicInteger to be thread-safe
     */
    static class ChargedTask {
        private final AtomicInteger stacks;
        private BukkitTask bukkitTask;

        public ChargedTask(AtomicInteger stacks, BukkitTask bukkitTask) {
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

