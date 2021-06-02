package com.runicrealms.plugin.api;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GearScanner;
import com.runicrealms.plugin.item.hearthstone.HearthstoneListener;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicItemShopManager;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.combat.CombatListener;
import com.runicrealms.plugin.player.mana.ManaListener;
import com.runicrealms.plugin.spellapi.PlayerSpellWrapper;
import com.runicrealms.plugin.spellapi.SpellUseListener;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RunicCoreAPI {

    /**
     * Allows other plugins to borrow the hearthstone teleportation mechanic w/ specified location.
     * @param player to teleport
     * @param location to arrive at
     */
    public static void beginTeleportation(Player player, Location location) {
        HearthstoneListener.beginTeleportation(player, location);
    }

    /**
     * Quickly grab a string representing the class the player is using, in lowercase!
     * @param player to grab class for
     * @return lowercase string representing their primary class
     */
    public static String getPlayerClass(Player player) {
        return RunicCoreAPI.getPlayerCache(player).getClassName().toLowerCase();
    }

    /**
     * Determine whether the player is in casting mode to cancel certain interactions.
     * @param player to check
     * @return boolean value, whether player is in casting set
     */
    public static boolean isCasting(Player player) {
        // determine whether player is casting
        return SpellUseListener.getCasters().containsKey(player.getUniqueId());
    }

    /**
     * Checks whether an entity is rooted.
     * @param entity to check
     * @return true if rooted
     */
    public static boolean isRooted(Entity entity) {
        return RunicCore.getSpellManager().getRootedEntites().containsKey(entity.getUniqueId());
    }

    /**
     * Checks whether an entity is silenced.
     * @param entity to check
     * @return true if silenced
     */
    public static boolean isSilenced(Entity entity) {
        return RunicCore.getSpellManager().getSilencedEntities().containsKey(entity.getUniqueId());
    }

    /**
     * Checks whether an entity is stunned.
     * @param entity to check
     * @return true if stunned
     */
    public static boolean isStunned(Entity entity) {
        return RunicCore.getSpellManager().getStunnedEntities().containsKey(entity.getUniqueId());
    }

    /**
     * Returns the base outlaw rating
     * @return the base rating (1500)
     */
    public static int getBaseOutlawRating() {
        return RunicCore.getBaseOutlawRating();
    }

    /**
     * Gets the MythicMobs item w/ internal name matching string
     * @param internalName internal name of item (NOT DISPLAY NAME)
     * @param amount of itemstack
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
     * @param internalName internal name of item (NOT DISPLAY NAME)
     * @param rand some Random object
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
     * Returns specified player cache in-memory for player
     * @param player to get cache for
     * @return a PlayerCache for player with wrapper data
     */
    public static PlayerCache getPlayerCache(Player player) {
        return RunicCore.getCacheManager().getPlayerCaches().get(player);
    }

    /**
     * Gets the spell in the associated 'slot' from player spell wrapper.
     * @param player to grab spell for
     * @param number of spell slot (1, 2, 3, 4)
     * @return a Spell object to be used elsewhere
     */
    public static Spell getPlayerSpell(Player player, int number) {
        Spell spellToCast = null;
        try {
            PlayerSpellWrapper playerSpellWrapper = RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player);
            switch (number) {
                case 1:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellWrapper.getSpellHotbarOne());
                    if (playerSpellWrapper.getSpellHotbarOne().equals(""))
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    break;
                case 2:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellWrapper.getSpellLeftClick());
                    if (playerSpellWrapper.getSpellLeftClick().equals(""))
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    break;
                case 3:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellWrapper.getSpellRightClick());
                    if (playerSpellWrapper.getSpellRightClick().equals(""))
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    break;
                case 4:
                    spellToCast = RunicCore.getSpellManager().getSpellByName(playerSpellWrapper.getSpellSwapHands());
                    if (playerSpellWrapper.getSpellSwapHands().equals(""))
                        player.sendMessage(ChatColor.RED + "You have no spell set in this slot!");
                    break;
            }
        } catch (NullPointerException e) {
            // haha sky is lazy
        }
        return spellToCast;
    }

    /**
     * Returns Skill Tree for specified player
     * @param player to lookup
     * @return Skill Tree
     */
    public static SkillTree getSkillTree(Player player, int position) {
        return RunicCore.getSkillTreeManager().searchSkillTree(player, position);
    }

    public static Spell getSpell(String name) {
        return RunicCore.getSpellManager().getSpellByName(name);
    }

    public static int getSpentPoints(Player player) {
        return RunicCore.getSkillTreeManager().getSpentPoints().get(player.getUniqueId());
    }

    /**
     * Used in Spell class to check if player has a passive applied!
     * @param player to check passive for
     * @param passive name of passive spell
     * @return boolean value whether passive found
     */
    public static boolean hasPassive(Player player, String passive) {
        return RunicCore.getSkillTreeManager().getPlayerSpellWrapper(player).getPassives().contains(passive);
    }

    /**
     * Returns simple boolean. If yes, player has party. If no, they don't.
     * @param player to check for party
     * @return true if party, false if none
     */
    public static boolean hasParty(Player player) {
        return RunicCore.getPartyManager().getPlayerParty(player) != null;
    }

    /**
     * Used to determine whether two players are in a party.
     * @param first The first player
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
     * @param player to build skill tree for
     * @param position the position of sub-class (1, 2, or 3)
     * @return SkillTreeGUI
     */
    public static SkillTreeGUI skillTreeGUI(Player player, int position) {
        if (RunicCore.getSkillTreeManager().searchSkillTree(player, position) != null)
            return new SkillTreeGUI(player, RunicCore.getSkillTreeManager().searchSkillTree(player, position));
        else
            return new SkillTreeGUI(player, new SkillTree(player, position));
    }

    /**
     * Check if the current player is in combat
     * @param player to check
     * @return true if player in combat
     */
    public static boolean isInCombat(Player player) {
        return RunicCore.getCombatManager().getPlayersInCombat().containsKey(player.getUniqueId());
    }

    /**
     * This is used in other plugins to manually tag a player in combat
     * @param damager the attacker
     * @param victim the defender
     */
    public static void tagCombat(Player damager, Entity victim) {
        CombatListener.tagCombat(damager, victim);
    }

    /**
     * This does.. um. Idek.
     * @param shop
     */
    public static void registerRunicItemShop(RunicItemShop shop) {
        RunicItemShopManager.registerShop(shop);
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
     * @param player player to calculate mana for
     */
    public static void updateMaxMana(Player player) {
        ManaListener.calculateMana(player);
    }

    /**
     * Prevents hunger loss in capital cities
     */
    public static boolean isSafezone(Location loc) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(loc));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return false;
        for (ProtectedRegion region : regions) {
            return cityNames().parallelStream().anyMatch(region.getId()::contains);
        }
        return false;
    }

    private static List<String> cityNames() {
        List<String> safeZones = new ArrayList<>();
        safeZones.add("azana");
        safeZones.add("koldore");
        safeZones.add("whaletown");
        safeZones.add("hilstead");
        safeZones.add("wintervale");
        safeZones.add("dawnshire");
        safeZones.add("dead_mans_rest");
        safeZones.add("isfodar");
        safeZones.add("tireneas");
        safeZones.add("zenyth");
        safeZones.add("naheen");
        safeZones.add("nazmora");
        safeZones.add("frosts_end");
        return safeZones;
    }
}
