package com.runicrealms.plugin.api;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.combat.CombatListener;
import com.runicrealms.plugin.spellapi.PlayerSpellWrapper;
import com.runicrealms.plugin.spellapi.SpellUseListener;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RunicCoreAPI {

    /**
     * Determine whether the player is in casting mode to cancel certain interactions.
     * @param player to check
     * @return boolean value, whether player is in casting set
     */
    public static boolean isCasting(Player player) {
        // determine whether player is casting
        return SpellUseListener.getCasters().contains(player.getUniqueId());
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
     * This is used in other plugins to... ugh... idek.
     * @param damager
     * @param victim
     */
    public static void tagCombat(Player damager, Entity victim) {
        CombatListener.tagCombat(damager, victim);
    }

    /**
     * This does.. um. Idek.
     * @param shop
     */
    public static void registerRunicItemShop(RunicItemShop shop) {
        RunicShopManager.registerShop(shop);
    }
}
