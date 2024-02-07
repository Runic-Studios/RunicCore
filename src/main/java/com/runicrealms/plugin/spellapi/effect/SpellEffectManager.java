package com.runicrealms.plugin.spellapi.effect;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.api.SpellEffectAPI;
import com.runicrealms.plugin.spellapi.effect.warrior.BleedEffect;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpellEffectManager implements Listener, SpellEffectAPI {
    private static final long TICK_PERIOD = 5; // Game ticks between each effect update (1/4 second)
    private final Set<SpellEffect> activeSpellEffects;
    private int counter = 0; // Add a counter field

    public SpellEffectManager() {
        activeSpellEffects = new HashSet<>();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), this::tickAll, 0L, TICK_PERIOD);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (hasSpellEffect(event.getPlayer().getUniqueId(), SpellEffectType.BLEED)) {
            event.setAmount((int) (event.getAmount() * (1 - BleedEffect.HEALING_REDUCTION))); // Receive less healing when bleeding
        }
    }

    public void tickAll() {
        counter += TICK_PERIOD; // Increment the counter

        Iterator<SpellEffect> iterator = activeSpellEffects.iterator();
        while (iterator.hasNext()) {
            SpellEffect effect = iterator.next();
            if (effect.isActive()) {
                effect.tick(counter);
            } else {
                effect.onExpire();
                iterator.remove();
                // Cleanup holograms
                if (effect instanceof StackEffect) {
                    ((StackEffect) effect).getStackHologram().getHologram().delete();
                }
            }
        }
    }

    @Override
    public void addSpellEffectToManager(SpellEffect spellEffect) {
        SpellEffectEvent spellEffectEvent = new SpellEffectEvent(spellEffect);
        Bukkit.getPluginManager().callEvent(spellEffectEvent);
        if (spellEffectEvent.isCancelled()) return;
        if (spellEffect instanceof StackEffect) {
            ((StackEffect) spellEffect).initializeNextTick(counter);
        }
        this.activeSpellEffects.add(spellEffect);
    }

    @Override
    public boolean hasSpellEffect(UUID uuid, SpellEffectType spellEffectType) {
        return activeSpellEffects.stream().anyMatch
                (
                        spellEffect -> spellEffect.getRecipient().getUniqueId().equals(uuid)
                                && spellEffect.getEffectType() == spellEffectType
                );
    }

    @Override
    public Optional<SpellEffect> getSpellEffect(UUID casterUuid, UUID recipientUuid, SpellEffectType identifier) {
        return activeSpellEffects.stream().filter
                (
                        spellEffect -> spellEffect.getCaster().getUniqueId().equals(casterUuid)
                                && spellEffect.getRecipient().getUniqueId().equals(recipientUuid)
                                && spellEffect.getEffectType() == identifier
                ).findFirst();
    }

    @Override
    public List<SpellEffect> getSpellEffects(UUID uuid, SpellEffectType identifier) {
        return activeSpellEffects.stream()
                .filter(spellEffect -> spellEffect.getRecipient().getUniqueId().equals(uuid)
                        && spellEffect.getEffectType() == identifier)
                .collect(Collectors.toList());
    }

    @Override
    public int determineHighestStacks(UUID recipientId, SpellEffectType identifier) {
        int result = 0; // Initialize result to 0

        // Get all spell effects for the given recipient and identifier
        List<SpellEffect> spellEffects = getSpellEffects(recipientId, identifier);

        // Iterate through the spell effects
        for (SpellEffect effect : spellEffects) {
            // Check if the effect is an instance of StackEffect
            if (effect instanceof StackEffect stackEffect) {
                // Get the max stacks for the stack effect
                int currentStacks = stackEffect.getStacks().get();
                // Update result if stacks is greater than the current result
                if (currentStacks > result) {
                    result = currentStacks;
                }
            }
        }

        return result; // Return the highest stacks found
    }

    public int getGlobalCounter() {
        return counter;
    }
}
