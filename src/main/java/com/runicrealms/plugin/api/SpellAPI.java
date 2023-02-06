package com.runicrealms.plugin.api;

import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
     * @param uuid of the player to lookup
     * @return a key set view of their spells which are on cooldown
     */
    @Nullable
    ConcurrentHashMap.KeySetView<Spell, Long> getSpellsOnCooldown(UUID uuid);

    /**
     * @param caster    who cast the spell
     * @param recipient to receive the healing
     * @param amount    amount to be healed before gem or buff calculations
     * @param spell     an optional reference to some spell for spell scaling
     */
    void healPlayer(Player caster, Player recipient, double amount, Spell... spell);

    /**
     * Determine whether the player is in casting mode to cancel certain interactions.
     *
     * @param player to check
     * @return boolean value, whether player is in casting set
     */
    boolean isCasting(Player player);

    /**
     * Check whether the current spell is on cooldown
     *
     * @param player    to check
     * @param spellName name of the spell
     * @return true if spell is on cooldown
     */
    boolean isOnCooldown(Player player, String spellName);

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

}
