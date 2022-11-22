package com.runicrealms.plugin.spellapi.spelltypes;

import com.runicrealms.plugin.classes.ClassEnum;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface ISpell {

    /**
     * Adds a custom status effect to an entity that interacts with runic systems, like silence preventing spells
     *
     * @param entity            to add effect to
     * @param runicStatusEffect the type of custom runic effect
     * @param duration          of the effect (in seconds)
     */
    void addStatusEffect(Entity entity, RunicStatusEffect runicStatusEffect, double duration);

    /**
     * Casts the actual spell effect
     *
     * @param player who cast the spell
     * @param type   the type of spell item (artifact usually now)
     */
    void execute(Player player, SpellItemType type);

    /**
     * @return The color associated with the spell. Typically, always WHITE
     */
    ChatColor getColor();

    /**
     * @return the cooldown of the spell, in seconds
     */
    double getCooldown();

    /**
     * @return a string description of the spell
     */
    String getDescription();

    /**
     * @return the mana cost of the spell
     */
    int getManaCost();

    /**
     * @return the name of the spell
     */
    String getName();

    /**
     * @return the class required to cast the spell
     */
    ClassEnum getReqClass();

    /**
     * Checks if the given player has the given passive
     *
     * @param uuid    of the player
     * @param passive name of the passive skill
     * @return true if the player has passive applied
     */
    boolean hasPassive(UUID uuid, String passive);

    /**
     * Check whether the entity is invulnerable by a custom runic effect
     *
     * @param entity to check
     * @return true if invulnerable
     */
    boolean isInvulnerable(Entity entity);

    /**
     * Check whether the current spell is on cooldown (use 'this')
     *
     * @param player to check
     * @return true if spell is on cooldown
     */
    boolean isOnCooldown(Player player);

    /**
     * Check whether the entity is rooted by a custom runic effect
     *
     * @param entity to check
     * @return true if rooted
     */
    boolean isRooted(Entity entity);

    /**
     * Check whether the entity is silenced by a custom runic effect
     *
     * @param entity to check
     * @return true if silenced
     */
    boolean isSilenced(Entity entity);

    /**
     * Check whether the entity is stunned by a custom runic effect
     *
     * @param entity to check
     * @return true if stunned
     */
    boolean isStunned(Entity entity);

    /**
     * Method to check for valid enemy before applying healing / buff spell calculation. True if enemy can be healed.
     *
     * @param caster    player who used spell
     * @param recipient entity who was hit by spell
     * @return whether target is valid
     */
    boolean isValidAlly(Player caster, Entity recipient);

    /**
     * Method to check for valid enemy before applying damage calculation. True if enemy can be damaged.
     *
     * @param caster player who used spell
     * @param victim mob or player who was hit by spell
     * @return whether target is valid
     */
    boolean isValidEnemy(Player caster, Entity victim); // check tons of things, like if target entity is NPC, party member, and outlaw checks

    /**
     * Used for execute skills that rely on percent missing health.
     *
     * @param entity  mob/player to check hp for
     * @param percent multiplier for missing health (.25 * missing health, etc.)
     * @return the percent times missing health
     */
    int percentMissingHealth(Entity entity, double percent);

    /**
     * Used to end custom Runic Effects on the target early by calling their cancel task
     *
     * @param entity            to remove effect from
     * @param runicStatusEffect the specified effect
     * @return true if an effect was removed
     */
    boolean removeStatusEffect(Entity entity, RunicStatusEffect runicStatusEffect);
}

