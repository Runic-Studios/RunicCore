package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.LootTableAPI;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.ThreadLocalRandom;

/**
 * A util to retrieve weighted loot tables for each tier of loot chest.
 */
public class LootTableManager implements LootTableAPI {

    /*
    Loot Chests
     */
    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_I;
    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_II;
    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_III;
    private static WeightedRandomBag<ChestItem> LOOT_TABLE_TIER_IV;

    /*
    Dungeons
     */
    private static WeightedRandomBag<ChestItem> SEBATHS_CAVE;
    private static WeightedRandomBag<ChestItem> CRYSTAL_CAVERN;
    private static WeightedRandomBag<ChestItem> JORUNDRS_KEEP;
    private static WeightedRandomBag<ChestItem> SUNKEN_LIBRARY;
    private static WeightedRandomBag<ChestItem> CRYPTS_OF_DERA;
    private static WeightedRandomBag<ChestItem> FROZEN_FORTRESS;


    static {
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            setupCommonLootTable();
            setupUncommonLootTable();
            setupRareLootTable();
            setupEpicLootTable();
            setupSebathsCaveLootTable();
            setupCrystalCavernLootTable();
            setupJorundrsKeepLootTable();
            setupSunkenLibraryLootTable();
            setupCryptsOfDeraLootTable();
            setupFrozenFortressLootTable();
        }, 10 * 20L);
    }

    private static void setupCommonLootTable() {
        // create a loot table object
        LOOT_TABLE_TIER_I = new WeightedRandomBag<>();

        // armor and weapons
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        ChestItem coin = new ChestItem("Coin", 3, 6);
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 1, 6);
        ChestItem goldOre = new ChestItem("gold-ore", 1, 6);
        ChestItem ironOre = new ChestItem("iron-ore", 1, 6);
        ChestItem bottle = new ChestItem("Bottle", 2, 3);
        ChestItem salmon = new ChestItem("Salmon", 2, 3);
        ChestItem comfrey = new ChestItem("Comfrey", 2, 3);
        ChestItem petunia = new ChestItem("Petunia", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("minor-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("minor-potion-mana", 1, 1);

        // add entries to table
        LOOT_TABLE_TIER_I.addEntry(randomArmorOrWeaponInLevelRange, 70.0);
        LOOT_TABLE_TIER_I.addEntry(coin, 50.0);
        LOOT_TABLE_TIER_I.addEntry(bread, 50.0);

        LOOT_TABLE_TIER_I.addEntry(spruceWood, 8.0);
        LOOT_TABLE_TIER_I.addEntry(oakWood, 8.0);
        LOOT_TABLE_TIER_I.addEntry(thread, 8.0);
        LOOT_TABLE_TIER_I.addEntry(animalHide, 8.0);
        LOOT_TABLE_TIER_I.addEntry(uncutRuby, 8.0);
        LOOT_TABLE_TIER_I.addEntry(goldOre, 8.0);
        LOOT_TABLE_TIER_I.addEntry(ironOre, 8.0);
        LOOT_TABLE_TIER_I.addEntry(bottle, 16.0);
        LOOT_TABLE_TIER_I.addEntry(salmon, 8.0);
        LOOT_TABLE_TIER_I.addEntry(comfrey, 8.0);
        LOOT_TABLE_TIER_I.addEntry(petunia, 8.0);

        LOOT_TABLE_TIER_I.addEntry(healthPotion, 20.0);
        LOOT_TABLE_TIER_I.addEntry(manaPotion, 20.0);
    }

    private static void setupUncommonLootTable() {
        // create a loot table object
        LOOT_TABLE_TIER_II = new WeightedRandomBag<>();

        // armor and weapons
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        // currency
        ChestItem coin = new ChestItem("Coin", 4, 8);

        // food
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 3, 8);
        ChestItem uncutSapphire = new ChestItem("uncut-sapphire", 3, 8);
        ChestItem goldOre = new ChestItem("gold-ore", 3, 6);
        ChestItem ironOre = new ChestItem("iron-ore", 3, 6);
        ChestItem bottle = new ChestItem("Bottle", 3, 5);
        ChestItem cod = new ChestItem("Cod", 2, 3);
        ChestItem comfrey = new ChestItem("Comfrey", 2, 3);
        ChestItem petunia = new ChestItem("Petunia", 2, 3);
        ChestItem turmeric = new ChestItem("Turmeric", 2, 3);
        ChestItem psyllium = new ChestItem("Psyllium", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("major-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("major-potion-mana", 1, 1);

        // add entries to table
        LOOT_TABLE_TIER_II.addEntry(randomArmorOrWeaponInLevelRange, 50.0);

        LOOT_TABLE_TIER_II.addEntry(coin, 50.0);
        LOOT_TABLE_TIER_II.addEntry(bread, 35.0);

        LOOT_TABLE_TIER_II.addEntry(spruceWood, 8.0);
        LOOT_TABLE_TIER_II.addEntry(oakWood, 8.0);
        LOOT_TABLE_TIER_II.addEntry(thread, 8.0);
        LOOT_TABLE_TIER_II.addEntry(animalHide, 8.0);
        LOOT_TABLE_TIER_II.addEntry(uncutRuby, 8.0);
        LOOT_TABLE_TIER_II.addEntry(uncutSapphire, 8.0);
        LOOT_TABLE_TIER_II.addEntry(goldOre, 8.0);
        LOOT_TABLE_TIER_II.addEntry(ironOre, 8.0);
        LOOT_TABLE_TIER_II.addEntry(bottle, 16.0);
        LOOT_TABLE_TIER_II.addEntry(cod, 8.0);
        LOOT_TABLE_TIER_II.addEntry(comfrey, 8.0);
        LOOT_TABLE_TIER_II.addEntry(petunia, 8.0);
        LOOT_TABLE_TIER_II.addEntry(turmeric, 8.0);
        LOOT_TABLE_TIER_II.addEntry(psyllium, 8.0);

        LOOT_TABLE_TIER_II.addEntry(healthPotion, 25.0);
        LOOT_TABLE_TIER_II.addEntry(manaPotion, 25.0);
    }

    private static void setupRareLootTable() {
        // create a loot table object
        LOOT_TABLE_TIER_III = new WeightedRandomBag<>();

        // armor and weapons
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        // currency
        ChestItem coin = new ChestItem("Coin", 5, 10);

        // food
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // crafting materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 3, 8);
        ChestItem uncutSapphire = new ChestItem("uncut-sapphire", 3, 8);
        ChestItem uncutOpal = new ChestItem("uncut-opal", 3, 8);
        ChestItem goldOre = new ChestItem("gold-ore", 3, 8);
        ChestItem ironOre = new ChestItem("iron-ore", 3, 8);
        ChestItem bottle = new ChestItem("Bottle", 3, 5);
        ChestItem tropical = new ChestItem("Tropical", 2, 3);
        ChestItem valerian = new ChestItem("Valerian", 2, 3);
        ChestItem snowdrop = new ChestItem("Snowdrop", 2, 3);
        ChestItem chamomile = new ChestItem("Chamomile", 2, 3);
        ChestItem wintercress = new ChestItem("Wintercress", 2, 3);
        ChestItem hibiscus = new ChestItem("Hibiscus", 2, 3);
        ChestItem lavender = new ChestItem("Lavender", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("major-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("major-potion-mana", 1, 1);

        // add entries to table
        LOOT_TABLE_TIER_III.addEntry(randomArmorOrWeaponInLevelRange, 50.0);
        LOOT_TABLE_TIER_III.addEntry(coin, 50.0);
        LOOT_TABLE_TIER_III.addEntry(bread, 35.0);

        LOOT_TABLE_TIER_III.addEntry(spruceWood, 8.0);
        LOOT_TABLE_TIER_III.addEntry(oakWood, 8.0);
        LOOT_TABLE_TIER_III.addEntry(thread, 8.0);
        LOOT_TABLE_TIER_III.addEntry(animalHide, 8.0);
        LOOT_TABLE_TIER_III.addEntry(uncutRuby, 8.0);
        LOOT_TABLE_TIER_III.addEntry(uncutSapphire, 8.0);
        LOOT_TABLE_TIER_III.addEntry(uncutOpal, 8.0);
        LOOT_TABLE_TIER_III.addEntry(goldOre, 8.0);
        LOOT_TABLE_TIER_III.addEntry(ironOre, 8.0);
        LOOT_TABLE_TIER_III.addEntry(bottle, 16.0);
        LOOT_TABLE_TIER_III.addEntry(tropical, 8.0);
        LOOT_TABLE_TIER_III.addEntry(valerian, 8.0);
        LOOT_TABLE_TIER_III.addEntry(snowdrop, 8.0);
        LOOT_TABLE_TIER_III.addEntry(chamomile, 8.0);
        LOOT_TABLE_TIER_III.addEntry(wintercress, 8.0);
        LOOT_TABLE_TIER_III.addEntry(hibiscus, 8.0);
        LOOT_TABLE_TIER_III.addEntry(lavender, 8.0);

        LOOT_TABLE_TIER_III.addEntry(healthPotion, 30.0);
        LOOT_TABLE_TIER_III.addEntry(manaPotion, 30.0);
    }

    private static void setupEpicLootTable() {
        // create a loot table object
        LOOT_TABLE_TIER_IV = new WeightedRandomBag<>();

        // armor and weapons
        ChestItem randomArmorOrWeaponInLevelRange = new ChestItem();

        // currency
        ChestItem coin = new ChestItem("Coin", 6, 12);

        // food
        ChestItem bread = new ChestItem("Bread", 2, 4);

        // materials
        ChestItem spruceWood = new ChestItem("SpruceWood", 3, 5);
        ChestItem oakWood = new ChestItem("OakWood", 3, 5);
        ChestItem thread = new ChestItem("Thread", 3, 5);
        ChestItem animalHide = new ChestItem("AnimalHide", 3, 5);
        ChestItem uncutRuby = new ChestItem("uncut-ruby", 3, 8);
        ChestItem uncutSapphire = new ChestItem("uncut-sapphire", 3, 8);
        ChestItem uncutOpal = new ChestItem("uncut-opal", 3, 8);
        ChestItem uncutEmerald = new ChestItem("uncut-emerald", 3, 8);
        ChestItem uncutDiamond = new ChestItem("uncut-diamond", 3, 8);
        ChestItem goldOre = new ChestItem("gold-ore", 3, 8);
        ChestItem ironOre = new ChestItem("iron-ore", 3, 8);
        ChestItem bottle = new ChestItem("Bottle", 3, 5);
        ChestItem pufferfish = new ChestItem("Pufferfish", 2, 3);
        ChestItem boswellia = new ChestItem("Boswellia", 2, 3);
        ChestItem arugula = new ChestItem("Arugula", 2, 3);

        // potions
        ChestItem healthPotion = new ChestItem("greater-potion-healing", 1, 1);
        ChestItem manaPotion = new ChestItem("greater-potion-mana", 1, 1);

        // add entries to table
        LOOT_TABLE_TIER_IV.addEntry(randomArmorOrWeaponInLevelRange, 30.0);
        LOOT_TABLE_TIER_IV.addEntry(coin, 50.0);
        LOOT_TABLE_TIER_IV.addEntry(bread, 35.0);

        LOOT_TABLE_TIER_IV.addEntry(spruceWood, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(oakWood, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(thread, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(animalHide, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutRuby, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutSapphire, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutOpal, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutEmerald, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(uncutDiamond, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(goldOre, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(ironOre, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(boswellia, 8.0);
        LOOT_TABLE_TIER_IV.addEntry(arugula, 8.0);

        LOOT_TABLE_TIER_IV.addEntry(bottle, 16.0);
        LOOT_TABLE_TIER_IV.addEntry(pufferfish, 8.0);

        LOOT_TABLE_TIER_IV.addEntry(healthPotion, 30.0);
        LOOT_TABLE_TIER_IV.addEntry(manaPotion, 30.0);
    }

    private static void setupSebathsCaveLootTable() {
        // Build base drop table from tier 1 chest
        SEBATHS_CAVE = new WeightedRandomBag<>(LOOT_TABLE_TIER_I);

        // Armor
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-archer-helm", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-archer-chest", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-archer-leggings", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-archer-boots", 1, 1), 5);

        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-cleric-helm", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-cleric-chest", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-cleric-leggings", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-cleric-boots", 1, 1), 5);

        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-mage-helm", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-mage-chest", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-mage-leggings", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-mage-boots", 1, 1), 5);

        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-rogue-helm", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-rogue-chest", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-rogue-leggings", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-rogue-boots", 1, 1), 5);

        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-warrior-helm", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-warrior-chest", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-warrior-leggings", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("sebaths-cave-warrior-boots", 1, 1), 5);

        // Weapons
        SEBATHS_CAVE.addEntry(new ChestItem("sanguine-longbow", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("crimson-maul", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("bloodmoon", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("scarlet-rapier", 1, 1), 5);
        SEBATHS_CAVE.addEntry(new ChestItem("corruption", 1, 1), 5);
    }

    private static void setupCrystalCavernLootTable() {
        // Build base drop table from tier 2 chest
        CRYSTAL_CAVERN = new WeightedRandomBag<>(LOOT_TABLE_TIER_II);

        // Armor
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-archer-helm", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-archer-chest", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-archer-leggings", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-archer-boots", 1, 1), 5);

        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-cleric-helm", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-cleric-chest", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-cleric-leggings", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-cleric-boots", 1, 1), 5);

        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-mage-helm", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-mage-chest", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-mage-leggings", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-mage-boots", 1, 1), 5);

        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-rogue-helm", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-rogue-chest", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-rogue-leggings", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-rogue-boots", 1, 1), 5);

        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-warrior-helm", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-warrior-chest", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-warrior-leggings", 1, 1), 5);
        CRYSTAL_CAVERN.addEntry(new ChestItem("crystal-cavern-warrior-boots", 1, 1), 5);
    }

    private static void setupJorundrsKeepLootTable() {
        // Build base drop table from tier 2 chest
        JORUNDRS_KEEP = new WeightedRandomBag<>(LOOT_TABLE_TIER_II);

        // Armor
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-archer-helm", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-archer-chest", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-archer-leggings", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-archer-boots", 1, 1), 5);

        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-cleric-helm", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-cleric-chest", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-cleric-leggings", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-cleric-boots", 1, 1), 5);

        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-mage-helm", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-mage-chest", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-mage-leggings", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-mage-boots", 1, 1), 5);

        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-rogue-helm", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-rogue-chest", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-rogue-leggings", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-rogue-boots", 1, 1), 5);

        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-warrior-helm", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-warrior-chest", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-warrior-leggings", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-keep-warrior-boots", 1, 1), 5);

        // Weapons
        JORUNDRS_KEEP.addEntry(new ChestItem("runeforged-piercer", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("runeforged-crusher", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("runeforged-scepter", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("lost-runeblade", 1, 1), 5);
        JORUNDRS_KEEP.addEntry(new ChestItem("jorundrs-wrath", 1, 1), 5);
    }

    private static void setupSunkenLibraryLootTable() {
        // Build base drop table from tier 3 chest
        SUNKEN_LIBRARY = new WeightedRandomBag<>(LOOT_TABLE_TIER_III);

        // Armor
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-archer-helm", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-archer-chest", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-archer-leggings", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-archer-boots", 1, 1), 5);

        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-cleric-helm", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-cleric-chest", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-cleric-leggings", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-cleric-boots", 1, 1), 5);

        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-mage-helm", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-mage-chest", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-mage-leggings", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-mage-boots", 1, 1), 5);

        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-rogue-helm", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-rogue-chest", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-rogue-leggings", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-rogue-boots", 1, 1), 5);

        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-warrior-helm", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-warrior-chest", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-warrior-leggings", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("sunken-library-warrior-boots", 1, 1), 5);

        // Weapons
        SUNKEN_LIBRARY.addEntry(new ChestItem("skeletal-shortbow", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("bonecleaver", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("ancient-arcane-rod", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("wolfspine", 1, 1), 5);
        SUNKEN_LIBRARY.addEntry(new ChestItem("deathbringer", 1, 1), 5);
    }

    private static void setupCryptsOfDeraLootTable() {
        // Build base drop table form tier 3 chest
        CRYPTS_OF_DERA = new WeightedRandomBag<>(LOOT_TABLE_TIER_III);

        // Armor
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-archer-helm", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-archer-chest", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-archer-leggings", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-archer-boots", 1, 1), 5);

        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-cleric-helm", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-cleric-chest", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-cleric-leggings", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-cleric-boots", 1, 1), 5);

        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-mage-helm", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-mage-chest", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-mage-leggings", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-mage-boots", 1, 1), 5);

        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-rogue-helm", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-rogue-chest", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-rogue-leggings", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-rogue-boots", 1, 1), 5);

        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-warrior-helm", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-warrior-chest", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-warrior-leggings", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("crypts-warrior-boots", 1, 1), 5);

        // Weapons
        CRYPTS_OF_DERA.addEntry(new ChestItem("triumph", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("gilded-impaler", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("prophets-cane", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("nightshade", 1, 1), 5);
        CRYPTS_OF_DERA.addEntry(new ChestItem("sandfury", 1, 1), 5);
    }

    private static void setupFrozenFortressLootTable() {
        // Build base drop table form tier 4 chest
        FROZEN_FORTRESS = new WeightedRandomBag<>(LOOT_TABLE_TIER_IV);

        // Armor
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-archer-helm", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-archer-chest", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-archer-leggings", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-archer-boots", 1, 1), 5);

        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-cleric-helm", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-cleric-chest", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-cleric-leggings", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-cleric-boots", 1, 1), 5);

        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-mage-helm", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-mage-chest", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-mage-leggings", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-mage-boots", 1, 1), 5);

        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-rogue-helm", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-rogue-chest", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-rogue-leggings", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-rogue-boots", 1, 1), 5);

        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-warrior-helm", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-warrior-chest", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-warrior-leggings", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frozen-fortress-warrior-boots", 1, 1), 5);

        // Weapons
        FROZEN_FORTRESS.addEntry(new ChestItem("winters-howl", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("chillrend", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("permafrost", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("blade-of-the-betrayer", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("frosts-edge", 1, 1), 5);
        FROZEN_FORTRESS.addEntry(new ChestItem("TokenOfValor", 1, 2), 15);
        FROZEN_FORTRESS.addEntry(new ChestItem("AmbrosiaRoot", 1, 4), 15);
    }

    @Override
    public ItemStack generateItemStack(ChestItem chestItem, BossChestTier bossChestTier) {
        if (chestItem.isScriptItem())
            return RunicItemsAPI.generateItemInRange(bossChestTier.getMinLootLevel(), bossChestTier.getMaxLootLevel(), 1).generateItem();
        String templateID = chestItem.getTemplateID();
        int minStackSize = chestItem.getMin();
        int maxStackSize = chestItem.getMax();
        // Bound is not inclusive, so we add 1
        return RunicItemsAPI.generateItemFromTemplate(templateID, (ThreadLocalRandom.current().nextInt(minStackSize, maxStackSize + 1))).generateItem();
    }

    @Override
    public ItemStack generateItemStack(ChestItem chestItem, LootChestTier lootChestTier) {
        if (chestItem.isScriptItem())
            return RunicItemsAPI.generateItemInRange(lootChestTier.getMinLootLevel(), lootChestTier.getMaxLootLevel(), 1).generateItem();
        String templateID = chestItem.getTemplateID();
        int minStackSize = chestItem.getMin();
        int maxStackSize = chestItem.getMax();
        // Bound is not inclusive, so we add 1
        return RunicItemsAPI.generateItemFromTemplate(templateID, (ThreadLocalRandom.current().nextInt(minStackSize, maxStackSize + 1))).generateItem();
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableCryptsOfDera() {
        return CRYPTS_OF_DERA;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableCrystalCavern() {
        return CRYSTAL_CAVERN;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableFrozenFortress() {
        return FROZEN_FORTRESS;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableJorundrsKeep() {
        return JORUNDRS_KEEP;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableSebathsCave() {
        return SEBATHS_CAVE;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableSunkenLibrary() {
        return SUNKEN_LIBRARY;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierI() {
        return LOOT_TABLE_TIER_I;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierII() {
        return LOOT_TABLE_TIER_II;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierIII() {
        return LOOT_TABLE_TIER_III;
    }

    @Override
    public WeightedRandomBag<ChestItem> getLootTableTierIV() {
        return LOOT_TABLE_TIER_IV;
    }
}
