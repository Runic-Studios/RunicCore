package com.runicrealms.plugin.api;

import com.runicrealms.plugin.spellapi.SpellSlot;
import com.runicrealms.plugin.spellapi.spelltypes.ShieldPayload;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
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
    void addCooldown(@NotNull Player player, @NotNull Spell spell, double cooldownTime);

    /**
     * Gets the current cooldown of the user's spell
     *
     * @param player to check
     * @param spell  to lookup
     * @return its remaining cooldown
     */
    double getUserCooldown(@NotNull Player player, @NotNull Spell spell);

    /**
     * Gets the spell in the associated 'slot' from player spell wrapper.
     *
     * @param player    to grab spell for
     * @param spellSlot which slot (swap hands, left click)
     * @return a Spell object to be used elsewhere
     */
    @Nullable
    Spell getPlayerSpell(@NotNull Player player, SpellSlot spellSlot);

    /**
     * Maps a player UUID to a shield object
     *
     * @return a map of players currently affected by shields
     */
    Map<UUID, ShieldPayload> getShieldedPlayers();

    /**
     * Adds duration to remaining cooldown for spell
     *
     * @param player       to add cooldown to
     * @param spell        to apply cooldown to
     * @param cooldownTime of spell
     */
    void increaseCooldown(@NotNull Player player, @NotNull Spell spell, double cooldownTime);

    /**
     * Adds duration to remaining cooldown for spell
     *
     * @param player       to add cooldown to
     * @param spell        to apply cooldown to
     * @param cooldownTime of spell
     */
    void increaseCooldown(@NotNull Player player, @NotNull String spell, double cooldownTime);

    /**
     * Get the spell object matching name
     *
     * @param name of the spell (e.g. "fireball")
     * @return the spell object
     */
    @Nullable
    Spell getSpell(@NotNull String name);

    /**
     * @param uuid of the player to lookup
     * @return a key set view of their spells which are on cooldown
     */
    @Nullable
    ConcurrentHashMap.KeySetView<Spell, Long> getSpellsOnCooldown(@NotNull UUID uuid);

    /**
     * @param caster    who cast the spell
     * @param recipient to receive the healing
     * @param amount    amount to be healed before gem or buff calculations
     * @param spell     an optional reference to some spell for spell scaling
     */
    void healPlayer(@NotNull Player caster, @NotNull Player recipient, double amount, @Nullable Spell spell);

    /**
     * @param caster    who cast the spell
     * @param recipient to receive the healing
     * @param amount    amount to be healed before gem or buff calculations
     */
    default void healPlayer(@NotNull Player caster, @NotNull Player recipient, double amount) {
        this.healPlayer(caster, recipient, amount, null);
    }

    /**
     * Determine whether the player is in casting mode to cancel certain interactions.
     *
     * @param player to check
     * @return boolean value, whether player is in casting set
     */
    boolean isCasting(@NotNull Player player);

    /**
     * Check whether the current spell is on cooldown
     *
     * @param player    to check
     * @param spellName name of the spell
     * @return true if spell is on cooldown
     */
    boolean isOnCooldown(@NotNull Player player, @NotNull String spellName);

    /**
     * Check if the current player is affected by a shield spell
     *
     * @param uuid of player to check
     * @return true if the player has a shield
     */
    boolean isShielded(@NotNull UUID uuid);

    /**
     * Reduces the cooldown of the given spell for the player by the duration (CDR)
     *
     * @param player   who cast the spell
     * @param spell    the spell to reduce CD for
     * @param duration the duration to reduce the CD (in seconds, so 0.5 for half sec)
     */
    void reduceCooldown(@NotNull Player player, @NotNull Spell spell, double duration);

    /**
     * Reduces the cooldown of the given spell for the player by the duration (CDR)
     *
     * @param player   who cast the spell
     * @param spell    the name of the spell to reduce CD for
     * @param duration the duration to reduce the CD (in seconds, so 0.5 for half sec)
     */
    void reduceCooldown(@NotNull Player player, @NotNull String spell, double duration);

    /**
     * Sets the cooldown of the given spell for the player to the duration
     *
     * @param player   who cast the spell
     * @param spell    the spell to reduce CD for
     * @param duration the duration to set the CD (in seconds, so 0.5 for half sec)
     */
    void setCooldown(@NotNull Player player, @NotNull Spell spell, double duration);

    /**
     * Sets the cooldown of the given spell for the player to the duration
     *
     * @param player   who cast the spell
     * @param spell    the name of the spell to reduce CD for
     * @param duration the duration to set the CD (in seconds, so 0.5 for half sec)
     */
    void setCooldown(@NotNull Player player, @NotNull String spell, double duration);

    /**
     * @param caster    who cast the spell
     * @param recipient to receive the shield
     * @param amount    amount to be healed before gem or buff calculations
     * @param spell     an optional reference to some spell for spell scaling
     */
    void shieldPlayer(@NotNull Player caster, @NotNull Player recipient, double amount, @Nullable Spell spell);

    /**
     * @param caster    who cast the spell
     * @param recipient to receive the shield
     * @param amount    amount to be healed before gem or buff calculations
     */
    default void shieldPlayer(@NotNull Player caster, @NotNull Player recipient, double amount) {
        this.shieldPlayer(caster, recipient, amount, null);
    }
}
