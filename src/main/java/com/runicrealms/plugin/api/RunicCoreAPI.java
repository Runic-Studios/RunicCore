package com.runicrealms.plugin.api;

import co.aikar.commands.PaperCommandManager;
import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicItemShopManager;
import com.runicrealms.plugin.listeners.HearthstoneListener;
import com.runicrealms.plugin.model.PlayerData;
import com.runicrealms.plugin.model.PlayerSpellData;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.player.listener.ManaListener;
import com.runicrealms.plugin.redis.RedisUtil;
import com.runicrealms.plugin.spellapi.SpellUseListener;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.Pair;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RunicCoreAPI {

    /**
     * Allows other plugins to borrow the hearthstone teleportation mechanic w/ specified location.
     *
     * @param player   to teleport
     * @param location to arrive at
     */
    public static void beginTeleportation(Player player, Location location) {
        HearthstoneListener.beginTeleportation(player, location);
    }

    /**
     * Checks whether the given location is within the given region
     *
     * @param location         to check
     * @param regionIdentifier the string identifier of region "azana"
     * @return true if the location is in the region
     */
    public static boolean containsRegion(Location location, String regionIdentifier) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return false;
        for (ProtectedRegion region : regions) {
            if (region.getId().contains(regionIdentifier))
                return true;
        }
        return false;
    }

    /**
     * Opens a new jedis resource which MUST BE CLOSED
     *
     * @return a jedis resource
     */
    public static Jedis getNewJedisResource() {
        return RunicCore.getRedisManager().getJedisResource();
    }

    /**
     * Quick method to grab player class from session data in redis
     *
     * @param player to lookup
     * @return a string representing the class (Cleric, Mage, etc.)
     */
    public static String getPlayerClass(Player player) {
        return RunicCore.getDatabaseManager().getLoadedCharactersMap().get(player.getUniqueId()).second.getName();
    }

    /**
     * Quick method to grab player class from session data in redis
     *
     * @param uuid of player to lookup
     * @return a string representing the class (Cleric, Mage, etc.)
     */
    public static String getPlayerClass(UUID uuid) {
        return RunicCore.getDatabaseManager().getLoadedCharactersMap().get(uuid).second.getName();
    }

    /**
     * Gets a copy of the PlayerData object from the database manager
     * NOTE: this object is destroyed once the player loads their character!
     * Only use it for login and select-based logic
     *
     * @param uuid of the player
     * @return their data wrapper object (no character data)
     */
    public static PlayerData getPlayerData(UUID uuid) {
        return RunicCore.getDatabaseManager().getPlayerDataMap().get(uuid);
    }

    /**
     * Returns the value in the key-value pair in redis (if it exists)
     * Used for account-wide fields
     *
     * @param uuid  of the player to lookup
     * @param field the key
     * @return the value
     */
    public static String getRedisPlayerValue(UUID uuid, String field, Jedis jedis) {
        return RedisUtil.getRedisValue(uuid, field, jedis);
    }

    /**
     * Returns the value in the key-value pair in redis (if it exists)
     * Used for character specific lookups
     *
     * @param uuid  of the player to lookup
     * @param field the key
     * @return the value
     */
    public static String getRedisCharacterValue(UUID uuid, String field, int slot, Jedis jedis) {
        return RedisUtil.getRedisValue(uuid, field, slot, jedis);
    }

    /**
     * Returns a map of values from session storage in redis as key-value pairs
     *
     * @param player the player to lookup
     * @param fields a list of constants
     * @return a map of key-value pairs
     */
    // todo: split into player, character
    public static Map<String, String> getRedisValues(Player player, List<String> fields, Jedis jedis) {
        return RedisUtil.getRedisValues(player, fields, jedis);
    }

    /**
     * Set the cached value in redis for the given player and key
     *
     * @param key   of the outer object in redis
     * @param field value of the key in the map
     * @param value to set
     * @return true if the key exists and was updated successfully
     */
    // todo: split into player, character
    public static boolean setRedisValue(String key, String field, String value, Jedis jedis) {
        return RedisUtil.setRedisValue(key, field, value, jedis);
    }

    /**
     * Set the cached value in redis for the given player and key
     *
     * @param player to lookup
     * @param field  value of the key
     * @param value  to set
     * @return true if the key exists and was updated successfully
     */
    // todo: split into player, character
    public static boolean setRedisValue(Player player, String field, String value, Jedis jedis) {
        return RedisUtil.setRedisValue(player, field, value, jedis);
    }

    /**
     * Set the cached values in redis for the given player and map of key-value pairs
     *
     * @param player
     * @param map
     * @return
     */
    // todo: split into player, character
    public static boolean setRedisValues(Player player, Map<String, String> map, Jedis jedis) {
        return RedisUtil.setRedisValues(player, map, jedis);
    }

    /**
     * Determine whether the player is in casting mode to cancel certain interactions.
     *
     * @param player to check
     * @return boolean value, whether player is in casting set
     */
    public static boolean isCasting(Player player) {
        // determine whether player is casting
        return SpellUseListener.getCasters().containsKey(player.getUniqueId());
    }

    /**
     * Checks whether an entity is rooted.
     *
     * @param entity to check
     * @return true if rooted
     */
    public static boolean isRooted(Entity entity) {
        return RunicCore.getSpellManager().getRootedEntites().containsKey(entity.getUniqueId());
    }

    /**
     * Checks whether an entity is silenced.
     *
     * @param entity to check
     * @return true if silenced
     */
    public static boolean isSilenced(Entity entity) {
        return RunicCore.getSpellManager().getSilencedEntities().containsKey(entity.getUniqueId());
    }

    /**
     * Checks whether an entity is stunned.
     *
     * @param entity to check
     * @return true if stunned
     */
    public static boolean isStunned(Entity entity) {
        return RunicCore.getSpellManager().getStunnedEntities().containsKey(entity.getUniqueId());
    }

    /**
     * Grab the core command manager to register ACF command from other plugins
     *
     * @return a PaperCommandManager
     */
    public static PaperCommandManager getPaperCommandManager() {
        return RunicCore.getCommandManager();
    }

    /**
     * Returns the base outlaw rating
     *
     * @return the base rating (1500)
     */
    public static int getBaseOutlawRating() {
        return RunicCore.getBaseOutlawRating();
    }

    public static ConcurrentHashMap.KeySetView<UUID, Pair<Integer, ClassEnum>> getLoadedCharacters() {
        return RunicCore.getDatabaseManager().getLoadedCharacters();
    }

    /**
     * Gets the MythicMobs item w/ internal name matching string
     *
     * @param internalName internal name of item (NOT DISPLAY NAME)
     * @param amount       of itemstack
     * @return an ItemStack
     */
    public static ItemStack getMythicItem(String internalName, int amount) {
        try {
            MythicItem mi = MythicMobs.inst().getItemManager().getItem(internalName).get();
            AbstractItemStack abstractItemStack = mi.generateItemStack(amount);
            return BukkitAdapter.adapt(abstractItemStack);
        } catch (NullPointerException e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "There was an error getting mythic item.");
            e.printStackTrace();
            return new ItemStack(Material.STONE);
        }
    }

    /**
     * Gets the MythicMobs item w/ internal name matching string, randomizes stack size
     *
     * @param internalName internal name of item (NOT DISPLAY NAME)
     * @param rand         some Random object
     * @param minStackSize minimum size of stack
     * @param maxStackSize max size of stack
     * @return an ItemStack
     */
    public static ItemStack getMythicItem(String internalName, Random rand, int minStackSize, int maxStackSize) {
        try {
            MythicItem mi = MythicMobs.inst().getItemManager().getItem(internalName).get();
            AbstractItemStack abstractItemStack = mi.generateItemStack(rand.nextInt(maxStackSize - minStackSize) + minStackSize);
            return BukkitAdapter.adapt(abstractItemStack);
        } catch (NullPointerException e) {
            Bukkit.getServer().getLogger().info(ChatColor.RED + "There was an error getting mythic item.");
            e.printStackTrace();
            return new ItemStack(Material.STONE);
        }
    }

    /**
     * Returns the currently selected character for the given player
     *
     * @param uuid of the player
     * @return an int representing their character slot (3, for example)
     */
    public static int getCharacterSlot(UUID uuid) {
        return RunicCore.getDatabaseManager().getLoadedCharactersMap().get(uuid).first;
    }

    /**
     * Return all the current passives mapped to the given player (by uuid)
     *
     * @param uuid of the player
     * @return a set of strings representing their passives
     */
    public static Set<String> getPassives(UUID uuid) {
        return RunicCore.getSkillTreeManager().getPlayerPassiveMap().get(uuid);
    }

    /**
     * Gets the spell in the associated 'slot' from player spell wrapper.
     *
     * @param player to grab spell for
     * @param number of spell slot (1, 2, 3, 4)
     * @return a Spell object to be used elsewhere
     */
    public static Spell getPlayerSpell(Player player, int number) {
        Spell spellToCast = null;
        UUID uuid = player.getUniqueId();
        try {
            PlayerSpellData playerSpellData = RunicCore.getSkillTreeManager().getPlayerSpellMap().get(uuid);
            switch (number) {
                case 1:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellData.getSpellHotbarOne());
                    if (playerSpellData.getSpellHotbarOne().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
                case 2:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellData.getSpellLeftClick());
                    if (playerSpellData.getSpellLeftClick().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
                case 3:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellData.getSpellRightClick());
                    if (playerSpellData.getSpellRightClick().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
                case 4:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellData.getSpellSwapHands());
                    if (playerSpellData.getSpellSwapHands().equals("")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    }
                    break;
            }
        } catch (NullPointerException e) {
            // haha sky is lazy
        }
        return spellToCast;
    }

    /**
     * Returns Skill Tree for specified player (from in memory cache)
     *
     * @param uuid     of player to lookup
     * @param position of the skill tree (1, 2, 3)
     * @return Skill Tree
     */
    public static SkillTreeData getSkillTree(UUID uuid, SkillTreePosition position) {
        return RunicCore.getSkillTreeManager().getPlayerSkillTreeMap().get(uuid + ":" + position.getValue());
    }

    public static Spell getSpell(String name) {
        return RunicCore.getSpellManager().getSpellByName(name);
    }

    /**
     * Gets the total skill points that are available to a given player that are NOT yet spent
     *
     * @param uuid of player to check
     * @return number of skill points availble (AFTER subtracting spent points)
     */
    public static int getAvailableSkillPoints(UUID uuid, int slot) {
        return SkillTreeData.getAvailablePoints(uuid);
    }

    /**
     * Gets the total allocated skill points of the given player
     *
     * @param uuid of player to check
     * @return number of skill points spent
     */
    public static int getSpentPoints(UUID uuid) {
        return RunicCore.getSkillTreeManager().getPlayerSpentPointsMap().get(uuid);
    }

    /**
     * Check if a player has required items (for quest or shops)
     *
     * @param player    to check
     * @param itemStack to check
     * @param amount    how many items do they need
     * @return true if player has the items
     */
    public static boolean hasItems(Player player, ItemStack itemStack, int amount) {
        return RunicItemShopManager.hasItems(player, itemStack, amount);
    }

    /**
     * Used in Spell class to check if player has a passive applied!
     *
     * @param uuid    of player to check passive for
     * @param passive name of passive spell
     * @return boolean value whether passive found
     */
    public static boolean hasPassive(UUID uuid, String passive) {
        return RunicCore.getSkillTreeManager().getPlayerPassiveMap().get(uuid).contains(passive);
    }

    /**
     * Returns simple boolean. If yes, player has party. If no, they don't.
     *
     * @param player to check for party
     * @return true if party, false if none
     */
    public static boolean hasParty(Player player) {
        return RunicCore.getPartyManager().getPlayerParty(player) != null;
    }

    /**
     * Used to determine whether two players are in a party.
     *
     * @param first  The first player
     * @param second The second player
     * @return boolean, whether they are in the same party
     */
    public static boolean isPartyMember(Player first, Player second) {
        if (RunicCore.getPartyManager().getPlayerParty(first) == null) return false;
        if (RunicCore.getPartyManager().getPlayerParty(second) == null) return false;
        return RunicCore.getPartyManager().getPlayerParty(first).hasMember(second);
    }

    public static RuneGUI runeGUI(Player player) {
        return new RuneGUI(player);
    }

    /**
     * Returns a SkillTreeGUI for the given player
     *
     * @param player   of player to build skill tree for
     * @param position the position of sub-class (1, 2, or 3)
     * @return SkillTreeGUI
     */
    public static SkillTreeGUI skillTreeGUI(Player player, SkillTreePosition position) {
        UUID uuid = player.getUniqueId();
        SkillTreeData skillTreeData = RunicCore.getSkillTreeManager().getPlayerSkillTreeMap().get(uuid + ":" + position.getValue());
        if (skillTreeData != null)
            return new SkillTreeGUI(player, skillTreeData);
        else
            return new SkillTreeGUI(player, new SkillTreeData(uuid, position));
    }

    /**
     * Check if the current player is in combat
     *
     * @param player to check
     * @return true if player in combat
     */
    public static boolean isInCombat(Player player) {
        return RunicCore.getCombatManager().getPlayersInCombat().containsKey(player.getUniqueId());
    }

    /**
     * Registers a RunicItemShop in our in-memory collection
     *
     * @param shop to register
     */
    public static void registerRunicItemShop(RunicItemShop shop) {
        RunicItemShopManager.registerRunicItemShop(shop);
    }

    public static int getPlayerDexterity(UUID uuid) {
        if (RunicCore.getStatManager().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return RunicCore.getStatManager().getPlayerStatContainer(uuid).getDexterity() + GearScanner.getItemDexterity(uuid);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getPlayerIntelligence(UUID uuid) {
        if (RunicCore.getStatManager().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return RunicCore.getStatManager().getPlayerStatContainer(uuid).getIntelligence() + GearScanner.getItemIntelligence(uuid);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getPlayerStrength(UUID uuid) {
        if (RunicCore.getStatManager().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return RunicCore.getStatManager().getPlayerStatContainer(uuid).getStrength() + GearScanner.getItemStrength(uuid);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getPlayerVitality(UUID uuid) {
        if (RunicCore.getStatManager().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return RunicCore.getStatManager().getPlayerStatContainer(uuid).getVitality() + GearScanner.getItemVitality(uuid);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getPlayerWisdom(UUID uuid) {
        if (RunicCore.getStatManager().getPlayerStatContainer(uuid) == null) return 0;
        try {
            return RunicCore.getStatManager().getPlayerStatContainer(uuid).getWisdom() + GearScanner.getItemWisdom(uuid);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Update the appropriate max mana for given player, based on gear, level, and stats.
     *
     * @param player player to calculate mana for
     */
    public static int calculateMaxMana(Player player) {
        return ManaListener.calculateMaxMana(player);
    }


    /**
     * Attempts to grab a dungeon location from the current location by checking the current region name
     * Returns null if no dungeon is found
     *
     * @param location of the player or entity
     * @return a dungeon location if found
     */
    public static DungeonLocation getDungeonFromLocation(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return null;
        for (ProtectedRegion region : regions) {
            for (DungeonLocation dungeonLocation : DungeonLocation.values()) {
                if (region.getId().contains(dungeonLocation.getIdentifier()))
                    return dungeonLocation;
            }
        }
        return null;
    }

    /**
     * Returns a list of the names of all regions containing the given location
     *
     * @param location the location to query
     * @return a list of region names
     */
    public static List<String> getRegionIds(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return new ArrayList<>();
        List<String> regionIds = new ArrayList<>();
        for (ProtectedRegion region : regions) {
            regionIds.add(region.getId());
        }
        return regionIds;
    }

    /**
     * Checks whether the given location is within a city
     *
     * @param location to check
     * @return true if it's within a city
     */
    public static boolean isSafezone(Location location) {
        List<String> regionIds = getRegionIds(location);
        for (String regionId : regionIds) {
            if (Arrays.stream(CityLocation.values()).anyMatch(cityLocation -> regionId.contains(cityLocation.getIdentifier())))
                return true;
        }
        return false;
    }

    /**
     * Used so that other plugins can trigger a scoreboard update
     *
     * @param player the player to update
     */
    public static void updatePlayerScoreboard(Player player) {
        RunicCore.getScoreboardHandler().updatePlayerInfo(player, player.getScoreboard());
    }
}
