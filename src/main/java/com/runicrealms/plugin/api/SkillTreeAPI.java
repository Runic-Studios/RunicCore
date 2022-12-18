package com.runicrealms.plugin.api;

import com.runicrealms.plugin.model.PlayerSpellData;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import org.bukkit.entity.Player;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface SkillTreeAPI {

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
     * @return a map of memoized skill tree data, keyed by uuid + ":" + position
     */
    Map<String, SkillTreeData> getPlayerSkillTreeMap();

    /**
     * @return a map of uuid to SpellData wrapper, which keeps track of which spell is assigned it which slot
     */
    Map<UUID, PlayerSpellData> getPlayerSpellMap();

    /**
     * @param jedis the jedis resource from the select event
     */
    SkillTreeData getSkillTree(UUID uuid, int slot, SkillTreePosition position, Jedis jedis);

    /**
     * Returns Skill Tree for specified player (from in memory cache)
     *
     * @param uuid     of player to lookup
     * @param slot     of the character
     * @param position of the skill tree (1, 2, 3)
     * @return Skill Tree
     */
    SkillTreeData getSkillTree(UUID uuid, int slot, SkillTreePosition position);

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
     * Creates a PlayerSpellData object. Tries to build it from session storage (Redis) first,
     * then falls back to Mongo
     *
     * @param uuid of player who is attempting to load their data
     * @param slot the slot of the character
     */
    PlayerSpellData loadPlayerSpellData(UUID uuid, Integer slot);

    /**
     * Returns a SkillTreeGUI for the given player
     *
     * @param player   of player to build skill tree for
     * @param position the position of sub-class (1, 2, or 3)
     * @return SkillTreeGUI
     */
    SkillTreeGUI skillTreeGUI(Player player, SkillTreePosition position);
}
