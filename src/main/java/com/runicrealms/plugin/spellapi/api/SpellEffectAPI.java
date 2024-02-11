package com.runicrealms.plugin.spellapi.api;

import com.runicrealms.plugin.spellapi.effect.SpellEffect;
import com.runicrealms.plugin.spellapi.effect.SpellEffectType;

import java.util.List;
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

    /**
     * Spell effects are keyed by the uuid of the CASTER. This method retrieves a list of spell effects
     * on the RECIPIENT (regardless of who applied the effect).
     *
     * @param recipientId uuid of the LivingEntity recipient
     * @param identifier  to search for
     * @return a list of all applicable effects, or an empty list
     */
    List<SpellEffect> getSpellEffects(UUID recipientId, SpellEffectType identifier);

    /**
     * Uses the 'getSpellEffects' method to search for a stack effect of an identifier,
     * then returns the highest current stack value of all active effects on the recipient
     *
     * @param recipientId uuid of the LivingEntity recipient
     * @param identifier  to search for
     * @return the highest stack count of all active effects of identifier type
     */
    int determineHighestStacks(UUID recipientId, SpellEffectType identifier);
}
