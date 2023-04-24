package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.Set;
import java.util.UUID;

public interface SkillTreeAPI {

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the SpellData object
     *
     * @param uuid              of player to check
     * @param slot              of the character
     * @param skillTreePosition which position is the skill tree? first, second, etc.
     * @param jedis             the jedis resource
     * @return a SpellData object if it is found in redis
     */
    SkillTreeData checkRedisForSkillTreeData(UUID uuid, Integer slot, SkillTreePosition skillTreePosition, Jedis jedis);

    /**
     * Checks redis to see if the currently selected character's data is cached.
     * And if it is, returns the SpellData object
     *
     * @param uuid of player to check
     * @param slot of the character
     * @return a SpellData object if it is found in redis
     */
    SpellData checkRedisForSpellData(UUID uuid, Integer slot, Jedis jedis);

    /**
     * Gets the total skill points that are available to a given player that are NOT yet spent
     *
     * @param uuid of player to check
     * @return number of skill points available (AFTER subtracting spent points)
     */
    int getAvailableSkillPoints(UUID uuid, int slot);

    /**
     * Return all the current passives mapped to the given player (by uuid)
     *
     * @param uuid of the player
     * @return a set of strings representing their passives
     */
    Set<String> getPassives(UUID uuid);

    /**
     * @param uuid of player
     * @param slot of character
     * @return a SpellData wrapper, which keeps track of which spell is assigned it which slot
     */
    SpellData getPlayerSpellData(UUID uuid, int slot);

    /**
     * Gets the total allocated skill points of the given player
     *
     * @param uuid of player to check
     * @param slot of the character
     * @return number of skill points spent
     */
    int getSpentPoints(UUID uuid, int slot);

    /**
     * Used in Spell parent class to check if player has a passive applied!
     *
     * @param uuid    of player to check passive for
     * @param passive name of passive spell
     * @return boolean value whether passive found
     */
    boolean hasPassiveFromSkillTree(UUID uuid, String passive);

    /**
     * Returns Skill Tree for specified player (from in memory cache)
     *
     * @param uuid     of player to lookup
     * @param slot     of the character
     * @param position of the skill tree (1, 2, 3)
     * @return Skill Tree
     */
    SkillTreeData loadSkillTreeData(UUID uuid, int slot, SkillTreePosition position);

    /**
     * Creates a SpellData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    SpellData loadSpellData(UUID uuid, Integer slot, Jedis jedis, String playerClass);

    /**
     * Tries to retrieve a SpellData wrapper for the given character from memory, else return null
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    SpellData loadSpellDataFromMemory(UUID uuid, Integer slot, String playerClass);

    /**
     * Returns a SkillTreeGUI for the given player
     *
     * @param player   of player to build skill tree for
     * @param position the position of sub-class (1, 2, or 3)
     * @return SkillTreeGUI
     */
    SkillTreeGUI skillTreeGUI(Player player, SkillTreePosition position);
}
