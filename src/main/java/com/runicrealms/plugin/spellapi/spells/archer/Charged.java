package com.runicrealms.plugin.spellapi.spells.archer;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.events.MagicDamageEvent;
import com.runicrealms.plugin.events.SpellCastEvent;
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

public class Charged extends Spell {
    private static final int BASE_INT = 1;
    private static final int MAX_STACKS = 5;
    private static final int TIMEOUT = 6;
    private static final double INT_PER_LEVEL = 0.1;
    private final Map<UUID, ChargedTask> chargedMap = new HashMap<>();

    public Charged() {
        super("Charged",
                "Every time you cast a spell, gain " +
                        "additional magicʔ damage equal to (" + BASE_INT + " + &f" + INT_PER_LEVEL + "x &7lvl) of your &eIntelligenceʔ&7! " +
                        "This effect can stack up to " + MAX_STACKS + " times. " +
                        "After not casting a spell for " + TIMEOUT + "s, remove all stacks. " +
                        "When fully charged, you glow brightly!",
                ChatColor.WHITE, CharacterClass.ARCHER, 0, 0);
        this.setIsPassive(true);
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
                    () -> cleanupTask(event.getCaster()), TIMEOUT * 20L);
            chargedMap.put(event.getCaster().getUniqueId(), new ChargedTask(new AtomicInteger(1), bukkitTask));
        } else {
            AtomicInteger stacks = chargedMap.get(event.getCaster().getUniqueId()).getStacks();

            // Refresh stack duration
            BukkitTask bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(RunicCore.getInstance(),
                    () -> cleanupTask(event.getCaster()), TIMEOUT * 20L);
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
    }

    /**
     * @param player whose stacks have expired
     */
    private void cleanupTask(Player player) {
        chargedMap.remove(player.getUniqueId());
        player.setGlowing(false);
    }

    @EventHandler(priority = EventPriority.HIGHEST) // late
    public void onMagicDamage(MagicDamageEvent event) {
        if (event.isCancelled()) return;
        if (!chargedMap.containsKey(event.getPlayer().getUniqueId())) return;
        int damageToGrant = (int) (BASE_INT + (event.getPlayer().getLevel() * INT_PER_LEVEL));
        damageToGrant *= chargedMap.get(event.getPlayer().getUniqueId()).getStacks().get();
        event.setAmount(event.getAmount() + damageToGrant);
    }

    @EventHandler
    public void onSpellCast(SpellCastEvent event) {
        if (event.isCancelled()) return;
        if (!hasPassive(event.getCaster().getUniqueId(), this.getName())) return;
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

