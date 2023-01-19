package com.runicrealms.plugin.api;

import com.runicrealms.plugin.spellapi.spelltypes.RunicStatusEffect;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface SpellAPI {

    /**
     * Adds spell to player, spell cooldown map
     *
     * @param player       to add cooldown to
     * @param spell        to apply cooldown to
     * @param cooldownTime of spell
     */
    void addCooldown(final Player player, final Spell spell, double cooldownTime);

    /**
     * Add a custom status effect to an entity.
     *
     * @param entity            to be silenced
     * @param runicStatusEffect which status effect to add
     * @param durationInSecs    (in seconds) of effect
     */
    void addStatusEffect(Entity entity, RunicStatusEffect runicStatusEffect, double durationInSecs, boolean displayMessage);

    /**
     * Gets the spell in the associated 'slot' from player spell wrapper.
     *
     * @param player to grab spell for
     * @param number of spell slot (1, 2, 3, 4)
     * @return a Spell object to be used elsewhere
     */
    Spell getPlayerSpell(Player player, int number);

    /**
     * Get the spell object matching name
     *
     * @param name of the spell (e.g. "fireball")
     * @return the spell object
     */
    Spell getSpell(String name);

    /**
     * Determine whether the player is in casting mode to cancel certain interactions.
     *
     * @param player to check
     * @return boolean value, whether player is in casting set
     */
    boolean isCasting(Player player);

    /**
     * Check whether the entity is invulnerable by a custom runic effect
     *
     * @param entity to check
     * @return true if invulnerable
     */
    boolean isInvulnerable(Entity entity);

    /**
     * Check whether the current spell is on cooldown
     *
     * @param player    to check
     * @param spellName name of the spell
     * @return true if spell is on cooldown
     */
    boolean isOnCooldown(Player player, String spellName);

    /**
     * Checks whether an entity is rooted.
     *
     * @param entity to check
     * @return true if rooted
     */
    boolean isRooted(Entity entity);

    /**
     * Checks whether an entity is silenced.
     *
     * @param entity to check
     * @return true if silenced
     */
    boolean isSilenced(Entity entity);

    /**
     * Checks whether an entity is stunned.
     *
     * @param entity to check
     * @return true if stunned
     */
    boolean isStunned(Entity entity);

    /**
     * Reduces the cooldown of the given spell for the player by the duration (CDR)
     *
     * @param player   who cast the spell
     * @param spell    the spell to reduce CD for
     * @param duration the duration to reduce the CD (in seconds, so 0.5 for half sec)
     */
    void reduceCooldown(Player player, Spell spell, double duration);

    /**
     * Reduces the cooldown of the given spell for the player by the duration (CDR)
     *
     * @param player   who cast the spell
     * @param spell    the name of the spell to reduce CD for
     * @param duration the duration to reduce the CD (in seconds, so 0.5 for half sec)
     */
    void reduceCooldown(Player player, String spell, double duration);

    /**
     * Removes the custom status effect (root, stun, etc.) from the specified player
     *
     * @param uuid         of the player to remove
     * @param statusEffect the status effect enum
     * @return true if an effect was removed
     */
    boolean removeStatusEffect(UUID uuid, RunicStatusEffect statusEffect);
}
