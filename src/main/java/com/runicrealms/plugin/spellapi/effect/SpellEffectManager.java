package com.runicrealms.plugin.spellapi.effect;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.api.SpellEffectAPI;
import com.runicrealms.plugin.spellapi.effect.event.SpellEffectEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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

    public int getGlobalCounter() {
        return counter;
    }
}
