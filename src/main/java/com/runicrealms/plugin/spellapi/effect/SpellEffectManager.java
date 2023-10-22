package com.runicrealms.plugin.spellapi.effect;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.events.SpellHealEvent;
import com.runicrealms.plugin.spellapi.api.SpellEffectAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class SpellEffectManager implements Listener, SpellEffectAPI {
    private static final long TICK_PERIOD = 5; // Game ticks
    private final Set<SpellEffect> activeSpellEffects;
    private int counter = 0; // Add a counter field

    public SpellEffectManager() {
        activeSpellEffects = new HashSet<>();
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().runTaskTimer(RunicCore.getInstance(), this::tickAll, 0L, TICK_PERIOD);
    }

    @EventHandler
    public void onSpellHeal(SpellHealEvent event) {
        if (hasSpellEffect(event.getPlayer().getUniqueId(), BleedEffect.IDENTIFIER)) {
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
            }
        }
    }

    @Override
    public void addSpellEffectToManager(SpellEffect spellEffect) {
        this.activeSpellEffects.add(spellEffect);
    }

    @Override
    public boolean hasSpellEffect(UUID uuid, String identifier) {
        return activeSpellEffects.stream().anyMatch
                (
                        spellEffect -> spellEffect.getRecipient().getUniqueId().equals(uuid)
                                && spellEffect.getIdentifier().equalsIgnoreCase(identifier)
                );
    }

    @Override
    public Optional<SpellEffect> getSpellEffect(UUID casterUuid, UUID recipientUuid, String identifier) {
        return activeSpellEffects.stream().filter
                (
                        spellEffect -> spellEffect.getCaster().getUniqueId().equals(casterUuid)
                                && spellEffect.getRecipient().getUniqueId().equals(recipientUuid)
                                && spellEffect.getIdentifier().equalsIgnoreCase(identifier)
                ).findFirst();
    }
}
