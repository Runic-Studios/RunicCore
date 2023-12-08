package com.runicrealms.plugin.spellapi.api;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;

import java.util.Optional;
import java.util.UUID;

public interface SpellEffectAPI {

    /**
     * Adds a custom SpellEffect to the spell manager
     *
     * @param spellEffect to add
     */
    void addSpellEffectToManager(SpellEffect spellEffect);

    /**
     * Checks if the given entity is affected by a SpellEffect with identifier
     *
     * @param uuid            of the entity
     * @param spellEffectType of the spell effect e.g. Bleed
     * @return true if the entity is affected
     */
    boolean hasSpellEffect(UUID uuid, SpellEffectType spellEffectType);

    /**
     * Returns a spell effect with the given caster and recipient (if it exists)
     *
     * @param caster     of the effect
     * @param recipient  of the effect
     * @param identifier of the effect (Bleed)
     * @return a spell effect
     */
    Optional<SpellEffect> getSpellEffect(UUID caster, UUID recipient, SpellEffectType identifier);
}
