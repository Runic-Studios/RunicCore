package com.runicrealms.plugin.api;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.shops.RunicItemShop;
import com.runicrealms.plugin.item.shops.RunicShopManager;
import com.runicrealms.plugin.player.cache.PlayerCache;
import com.runicrealms.plugin.player.combat.CombatListener;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import com.runicrealms.plugin.spellapi.skilltrees.gui.RuneGUI;
import com.runicrealms.plugin.spellapi.skilltrees.gui.SkillTreeGUI;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.adapters.AbstractItemStack;
import io.lumine.xikage.mythicmobs.adapters.bukkit.BukkitAdapter;
import io.lumine.xikage.mythicmobs.items.MythicItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

public class RunicCoreAPI {

    /**
     * Returns the base outlaw rating
     * @return the base rating (1500)
     */
    public static int getBaseOutlawRating() {
        return RunicCore.getBaseOutlawRating();
    }

    /**
     * Gets the MythicMobs item w/ internal name matching string
     * @param itemName internal name of item (NOT DISPLAY NAME)
     * @param amount of itemstack
     * @return an ItemStack
     */
    public static ItemStack getMythicItem(String itemName, int amount) {
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(itemName).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(amount);
        return BukkitAdapter.adapt(abstractItemStack);
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
        MythicItem mi = MythicMobs.inst().getItemManager().getItem(internalName).get();
        AbstractItemStack abstractItemStack = mi.generateItemStack(rand.nextInt(maxStackSize - minStackSize) + minStackSize);
        return BukkitAdapter.adapt(abstractItemStack);
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
     * Returns Skill Tree for specified player
     * @param player to lookup
     * @return Skill Tree
     */
    public static SkillTree getSkillTree(Player player) {
        return RunicCore.getSkillTreeManager().getSkillTree(player);
    }

    public static Spell getSpell(String name) {
        return RunicCore.getSpellManager().getSpellByName(name);
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
        if (RunicCore.getSkillTreeManager().getSkillTree(player) != null)
            return new SkillTreeGUI(player, RunicCore.getSkillTreeManager().getSkillTree(player));
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
