package com.runicrealms.plugin.item.util;

import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.item.ItemNameGenerator;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.item.TieredItemGenerator;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ItemUtils {

    public static ItemStack generateCommonArmor() {

        // generate base info for item
        TieredItemGenerator tieredItemGenerator = new TieredItemGenerator();
        String itemTypeName = tieredItemGenerator.getItemTypeName();
        Material material = tieredItemGenerator.getMaterial();
        int durability = tieredItemGenerator.getDurability();
        String itemSlot = tieredItemGenerator.getItemSlot();

        // create our itemstack
        ItemStack commonItem = new ItemStack(material);
        ItemMeta meta = commonItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        commonItem.setItemMeta(meta);

        Random rand = new Random();
        int numOfStats = rand.nextInt(2) + 1;

        int maxHealth = 0;
        int maxMana = 0;
        String className = TieredItemGenerator.determineClass(commonItem, material);
        switch (className.toLowerCase()) {
            case "mage":
                maxHealth = 10;
                maxMana = 10;
                break;
            case "rogue":
                maxHealth = 10;
                maxMana = 10;
                break;
            case "archer":
                maxHealth = 10;
                maxMana = 10;
                break;
            case "cleric":
                maxHealth = 20;
                maxMana = 10;
                break;
            case "warrior":
                maxHealth = 20;
                maxMana = 10;
                break;
        }

        int health = rand.nextInt(maxHealth - (maxHealth/2)) + (maxHealth/2);
        int mana = rand.nextInt(maxMana - (maxMana/2)) + (maxMana/2);

        commonItem = AttributeUtil.addCustomStat(commonItem, "custom.maxHealth", health);
        commonItem = AttributeUtil.addGenericStat(commonItem, "generic.armor", 0, itemSlot);

        if (numOfStats == 1) {
            commonItem = AttributeUtil.addCustomStat(commonItem, "custom.manaBoost", mana);
        }

        ItemNameGenerator nameGen = new ItemNameGenerator();
        String name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf("COMMON"));
        LoreGenerator.generateItemLore(commonItem, ChatColor.GRAY, name + " " + itemTypeName, "", false);

        return commonItem;
    }

    public static ItemStack generateUncommonArmor() {

        // generate base info for item
        TieredItemGenerator tieredItemGenerator = new TieredItemGenerator();
        String itemTypeName = tieredItemGenerator.getItemTypeName();
        Material material = tieredItemGenerator.getMaterial();
        int durability = tieredItemGenerator.getDurability();
        String itemSlot = tieredItemGenerator.getItemSlot();

        // create our itemstack
        ItemStack uncommonItem = new ItemStack(material);
        ItemMeta meta = uncommonItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        uncommonItem.setItemMeta(meta);

        // set minimum level
        uncommonItem = AttributeUtil.addCustomStat(uncommonItem, "required.level", 10);

        Random rand = new Random();
        int numOfStats = rand.nextInt(2) + 1;

        int health = 0;
        int mana = 0;
        int weapDamage = rand.nextInt(2-1) + 1;

        String className = TieredItemGenerator.determineClass(uncommonItem, material);
        switch (className.toLowerCase()) {
            case "mage":
                health = rand.nextInt(15-10) + 10;
                mana = rand.nextInt(15-10) + 10;
                break;
            case "rogue":
                health = rand.nextInt(15-10) + 10;
                mana = rand.nextInt(15-10) + 10;
                break;
            case "archer":
                health = rand.nextInt(15-10) + 10;
                mana = rand.nextInt(15-10) + 10;
                break;
            case "cleric":
                health = rand.nextInt(30-20) + 20;
                mana = rand.nextInt(15-10) + 10;
                break;
            case "warrior":
                health = rand.nextInt(30-20) + 20;
                mana = rand.nextInt(15-10) + 10;
                break;
        }

        List<Integer> stats = determineWhichStats(numOfStats, 3); // 1 or 2 stats in addition to health

        uncommonItem = AttributeUtil.addCustomStat(uncommonItem, "custom.maxHealth", health);
        uncommonItem = AttributeUtil.addGenericStat(uncommonItem, "generic.armor", 0, itemSlot);

        if (stats.contains(2)) {
            uncommonItem = AttributeUtil.addCustomStat(uncommonItem, "custom.manaBoost", mana);
        }
        if (stats.contains(3)) {
            uncommonItem = AttributeUtil.addCustomStat(uncommonItem, "custom.attackDamage", weapDamage);
        }

        ItemNameGenerator nameGen = new ItemNameGenerator();
        String name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf("UNCOMMON"));
        LoreGenerator.generateItemLore(uncommonItem, ChatColor.GREEN, name + " " + itemTypeName, "", false);

        return uncommonItem;
    }

    public static ItemStack generateRareArmor() {

        // generate base info for item
        TieredItemGenerator tieredItemGenerator = new TieredItemGenerator();
        String itemTypeName = tieredItemGenerator.getItemTypeName();
        Material material = tieredItemGenerator.getMaterial();
        int durability = tieredItemGenerator.getDurability();
        String itemSlot = tieredItemGenerator.getItemSlot();

        // create our itemstack
        ItemStack rareItem = new ItemStack(material);
        ItemMeta meta = rareItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        rareItem.setItemMeta(meta);

        // set required level
        rareItem = AttributeUtil.addCustomStat(rareItem, "required.level", 25);

        // item can be socketed once
        rareItem = AttributeUtil.addCustomStat(rareItem, "custom.socketCount", 1);

        Random rand = new Random();

        int health = 0;
        int mana = 0;
        int weapDamage = rand.nextInt(3-2) + 2; // 2-3
        int healing = 3;
        int spellDamage = rand.nextInt(4-2) + 2; // 2-4
        String className = TieredItemGenerator.determineClass(rareItem, material);
        switch (className.toLowerCase()) {
            case "mage":
                health = rand.nextInt(30-15) + 15;
                mana = rand.nextInt(30-15) + 15;
                break;
            case "rogue":
                health = rand.nextInt(30-15) + 15;
                mana = rand.nextInt(30-15) + 15;
                break;
            case "archer":
                health = rand.nextInt(30-15) + 15;
                mana = rand.nextInt(30-15) + 15;
                break;
            case "cleric":
                health = rand.nextInt(50-30) + 30;
                mana = rand.nextInt(30-15) + 15;
                break;
            case "warrior":
                health = rand.nextInt(50-30) + 30;
                mana = rand.nextInt(30-15) + 15;
                break;
        }

        List<Integer> stats = ItemUtils.determineWhichStats(2, 5);

        //  add health no matter what
        rareItem = AttributeUtil.addCustomStat(rareItem, "custom.maxHealth", health);
        rareItem = AttributeUtil.addGenericStat(rareItem, "generic.armor", 0, itemSlot);

        if (stats.contains(2)) {
            rareItem = AttributeUtil.addCustomStat(rareItem, "custom.manaBoost", mana);
        }
        if (stats.contains(3)) {
            rareItem = AttributeUtil.addCustomStat(rareItem, "custom.attackDamage", weapDamage);
        }
        if (stats.contains(4)) {
            rareItem = AttributeUtil.addCustomStat(rareItem, "custom.healingBoost", healing);
        }
        if (stats.contains(5)) {
            rareItem = AttributeUtil.addCustomStat(rareItem, "custom.magicDamage", spellDamage);
        }

        ItemNameGenerator nameGen = new ItemNameGenerator();
        String name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf("RARE"));
        LoreGenerator.generateItemLore(rareItem, ChatColor.AQUA, name + " " + itemTypeName, "", false);

        return rareItem;
    }

    public static ItemStack generateEpicArmor() {

        // generate base info for item
        TieredItemGenerator tieredItemGenerator = new TieredItemGenerator();
        String itemTypeName = tieredItemGenerator.getItemTypeName();
        Material material = tieredItemGenerator.getMaterial();
        int durability = tieredItemGenerator.getDurability();
        String itemSlot = tieredItemGenerator.getItemSlot();

        // create our itemstack
        ItemStack epicItem = new ItemStack(material);
        ItemMeta meta = epicItem.getItemMeta();
        ((Damageable) Objects.requireNonNull(meta)).setDamage(durability);
        epicItem.setItemMeta(meta);

        // set required level
        epicItem = AttributeUtil.addCustomStat(epicItem, "required.level", 40);

        // item can be socketed once
        epicItem = AttributeUtil.addCustomStat(epicItem, "custom.socketCount", 1);

        Random rand = new Random();

        int health = 0;
        int mana = 0;
        int weapDamage = rand.nextInt(5-3) + 3; // 3-5
        int healing = 4;
        int spellDamage = rand.nextInt(5-3) + 3; // 3-5
        String className = TieredItemGenerator.determineClass(epicItem, material);
        switch (className.toLowerCase()) {
            case "mage":
                health = rand.nextInt(50-30) + 30;
                mana = rand.nextInt(50-30) + 30;
                break;
            case "rogue":
                health = rand.nextInt(50-30) + 30;
                mana = rand.nextInt(50-30) + 30;
                break;
            case "archer":
                health = rand.nextInt(50-30) + 30;
                mana = rand.nextInt(50-30) + 30;
                break;
            case "cleric":
                health = rand.nextInt(80-50) + 50;
                mana = rand.nextInt(50-30) + 30;
                break;
            case "warrior":
                health = rand.nextInt(80-50) + 50;
                mana = rand.nextInt(50-30) + 30;
                break;
        }

        List<Integer> stats = ItemUtils.determineWhichStats(2, 5);

        //  add health no matter what
        epicItem = AttributeUtil.addCustomStat(epicItem, "custom.maxHealth", health);
        epicItem = AttributeUtil.addGenericStat(epicItem, "generic.armor", 0, itemSlot);

        if (stats.contains(2)) {
            epicItem = AttributeUtil.addCustomStat(epicItem, "custom.manaBoost", mana);
        }
        if (stats.contains(3)) {
            epicItem = AttributeUtil.addCustomStat(epicItem, "custom.attackDamage", weapDamage);
        }
        if (stats.contains(4)) {
            epicItem = AttributeUtil.addCustomStat(epicItem, "custom.healingBoost", healing);
        }
        if (stats.contains(5)) {
            epicItem = AttributeUtil.addCustomStat(epicItem, "custom.magicDamage", spellDamage);
        }

        ItemNameGenerator nameGen = new ItemNameGenerator();
        String name = nameGen.generateName(ItemNameGenerator.NameTier.valueOf("EPIC"));
        LoreGenerator.generateItemLore(epicItem, ChatColor.LIGHT_PURPLE, name + " " + itemTypeName, "", false);

        return epicItem;
    }

    private static List<Integer> determineWhichStats(int totalNumOfStats, int statUpTo) {

        // ex: if statUpTo is 3, it will add 1, 2, and 3, corresponding to +health, +mana, and +healing (order of gemstones)
        List<Integer> howManyDiffStatsDoWeHave = new ArrayList<>();
        for (int i = 1; i <= statUpTo; i++) {
            howManyDiffStatsDoWeHave.add(i);
        }

        Random rand = new Random();

        // create a temporary list for storing
        // selected element
        List<Integer> newList = new ArrayList<>();
        for (int i = 0; i < totalNumOfStats; i++) {

            // take a raundom index between 0 to size
            // of given List
            int bound = howManyDiffStatsDoWeHave.size() - 1;
            if (bound <= 0) {
                bound = 1;
            }
            int randomIndex = rand.nextInt(bound) + 1;

            // add element in temporary list
            newList.add(howManyDiffStatsDoWeHave.get(randomIndex));

            // Remove selected element from orginal list
            howManyDiffStatsDoWeHave.remove(randomIndex);
        }
        return newList;
    }

    /**
     * @param type display name of the item, accepts color codes
     * @param someVar whatever variable the potion takes goes here (health, mana, duration. the system knows)
     */
    public static ItemStack generatePotion(String type, int someVar) {

        String dispName = "";
        switch (type) {
            case "healing":
                // todo: fix this
                if (someVar <= 15) {
                    dispName = "&cNovice Potion of Healing";
                } else {
                    dispName = "&cPotion of Healing";
                }
                break;
            case "mana":
                if (someVar <= 15) {
                    dispName = "&3Novice Potion of Mana";
                } else {
                    dispName = "&3Potion of Mana";
                }
                break;
            case "slaying":
                dispName = "&b&oPotion of Slaying";
                break;
            case "looting":
                dispName = "&6Potion of Looting";
                break;
        }


        ItemStack potion = new ItemStack(Material.POTION);
        PotionMeta pMeta = (PotionMeta) potion.getItemMeta();
        Color color;
        String desc;
        if (dispName.toLowerCase().contains("healing")) {
            color = Color.RED;
            desc = "\n&eRestores &c" + someVar + "❤ &eon use";
        } else if (dispName.toLowerCase().contains("mana")) {
            color = Color.AQUA;
            desc = "\n&eRestores &3" + someVar + "✸ &eon use";
        } else if (dispName.toLowerCase().contains("slaying")) {
            color = Color.BLACK;
            desc = "\n&eIncreases spellʔ and weapon⚔ damage" +
                    "\n&evs. monsters by &f20% &efor &f" + someVar + " &eminutes";
        } else {
            color = Color.ORANGE;
            desc = "\n&eIncreases looting chance by &f20%" +
                    "\n&efor &f" + someVar + " &eminutes";
        }
        Objects.requireNonNull(pMeta).setColor(color);

        pMeta.setDisplayName(ColorUtil.format(dispName));
        ArrayList<String> lore = new ArrayList<>();
        for (String s : desc.split("\n")) {
            lore.add(ColorUtil.format(s));
        }
        lore.add("");
        lore.add(ColorUtil.format("&7Consumable"));
        pMeta.setLore(lore);

        pMeta.addEnchant(Enchantment.DURABILITY, 1, true);
        pMeta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        pMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        pMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        potion.setItemMeta(pMeta);

        // ----------------------------------------------
        // must be set AFTER meta is set
        if (dispName.toLowerCase().contains("healing")) {
            potion = AttributeUtil.addCustomStat(potion, "potion.healing", someVar);
        } else if (dispName.toLowerCase().contains("mana")) {
            potion = AttributeUtil.addCustomStat(potion, "potion.mana", someVar);
        } else if (dispName.toLowerCase().contains("slaying")) {
            potion = AttributeUtil.addCustomStat(potion, "potion.slaying", someVar);
        } else {
            potion = AttributeUtil.addCustomStat(potion, "potion.looting", someVar);
        }
        // ----------------------------------------------

        return potion;
    }
}
