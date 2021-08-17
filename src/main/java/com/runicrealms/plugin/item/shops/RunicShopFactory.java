package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.commands.TravelCMD;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Factory class to create generic runic shops for the server
 */
public class RunicShopFactory {

    public RunicShopFactory() {
        getAlchemistShop();
        getBagVendor();
        getBaker();
        getCaptain();
        getGeneralStore();
        getRunicMage();
        getWagonMaster();
        /*
        DUNGEON SHOPS
         */
        getCavernShop();
    }

    private final ItemStack bottle = RunicItemsAPI.generateItemFromTemplate("Bottle").generateItem();
    private final ItemStack minorHealingPotion = RunicItemsAPI.generateItemFromTemplate("minor-potion-healing").generateItem();
    private final ItemStack minorManaPotion = RunicItemsAPI.generateItemFromTemplate("minor-potion-mana").generateItem();
    private final ItemStack majorHealingPotion = RunicItemsAPI.generateItemFromTemplate("major-potion-healing").generateItem();
    private final ItemStack majorManaPotion = RunicItemsAPI.generateItemFromTemplate("major-potion-mana").generateItem();
    private final ItemStack greaterHealingPotion = RunicItemsAPI.generateItemFromTemplate("greater-potion-healing").generateItem();
    private final ItemStack greaterManaPotion = RunicItemsAPI.generateItemFromTemplate("greater-potion-mana").generateItem();

    public RunicShopGeneric getAlchemistShop() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(bottle, new RunicShopItem(2, "Coin", RunicShopGeneric.iconWithLore(bottle, 2)));
        shopItems.put(minorHealingPotion, new RunicShopItem(8, "Coin", RunicShopGeneric.iconWithLore(minorHealingPotion, 8)));
        shopItems.put(minorManaPotion, new RunicShopItem(8, "Coin", RunicShopGeneric.iconWithLore(minorManaPotion, 8)));
        shopItems.put(majorHealingPotion, new RunicShopItem(16, "Coin", RunicShopGeneric.iconWithLore(majorHealingPotion, 16)));
        shopItems.put(majorManaPotion, new RunicShopItem(16, "Coin", RunicShopGeneric.iconWithLore(majorManaPotion, 16)));
        shopItems.put(greaterHealingPotion, new RunicShopItem(24, "Coin", RunicShopGeneric.iconWithLore(greaterHealingPotion, 24)));
        shopItems.put(greaterManaPotion, new RunicShopItem(24, "Coin", RunicShopGeneric.iconWithLore(greaterManaPotion, 24)));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Alchemist", Arrays.asList(101, 103, 104, 105, 106, 107, 108, 109, 110, 111), shopItems);
    }

    private final ItemStack goldPouch = RunicItemsAPI.generateItemFromTemplate("gold-pouch").generateItem();

    public RunicShopGeneric getBagVendor() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(goldPouch, new RunicShopItem(128, "Coin", RunicShopGeneric.iconWithLore(goldPouch, 128)));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Bag Vendor", Arrays.asList(179, 181, 182, 183, 185, 186, 187, 189, 190, 192, 194), shopItems);
    }

    private final ItemStack bread = RunicItemsAPI.generateItemFromTemplate("Bread").generateItem();

    public RunicShopGeneric getBaker() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(bread, new RunicShopItem(4, "Coin", RunicShopGeneric.iconWithLore(bread, 4)));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Baker", Arrays.asList(115, 116, 118, 119, 120, 121, 124, 125, 126), shopItems);
    }

    private final ItemStack beetroot = RunicItemsAPI.generateItemFromTemplate("azanashop-beetroot").generateItem();
    private final ItemStack carrot = RunicItemsAPI.generateItemFromTemplate("azanashop-carrot").generateItem();
    private final ItemStack azanaShopArcherBow = RunicItemsAPI.generateItemFromTemplate("azanashop-archer-bow").generateItem();
    private final ItemStack azanaShopClericMace = RunicItemsAPI.generateItemFromTemplate("azanashop-cleric-mace").generateItem();
    private final ItemStack azanaShopMageStaff = RunicItemsAPI.generateItemFromTemplate("azanashop-mage-staff").generateItem();
    private final ItemStack azanaShopRogueSword = RunicItemsAPI.generateItemFromTemplate("azanashop-rogue-sword").generateItem();
    private final ItemStack azanaShopWarriorAxe = RunicItemsAPI.generateItemFromTemplate("azanashop-warrior-axe").generateItem();
    private final ItemStack azanaShopArcherBoots = RunicItemsAPI.generateItemFromTemplate("azanashop-archer-boots").generateItem();
    private final ItemStack azanaShopClericHelmet = RunicItemsAPI.generateItemFromTemplate("azanashop-cleric-helmet").generateItem();
    private final ItemStack azanaShopMageHelmet = RunicItemsAPI.generateItemFromTemplate("azanashop-mage-helmet").generateItem();
    private final ItemStack azanaShopRogueBoots = RunicItemsAPI.generateItemFromTemplate("azanashop-rogue-boots").generateItem();
    private final ItemStack azanaShopWarriorHelmet = RunicItemsAPI.generateItemFromTemplate("azanashop-warrior-helmet").generateItem();

    public RunicShopGeneric getGeneralStore() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(beetroot, new RunicShopItem(2, "Coin", RunicShopGeneric.iconWithLore(beetroot, 2)));
        shopItems.put(carrot, new RunicShopItem(3, "Coin", RunicShopGeneric.iconWithLore(carrot, 3)));
        shopItems.put(minorHealingPotion, new RunicShopItem(10, "Coin", RunicShopGeneric.iconWithLore(minorHealingPotion, 8)));
        shopItems.put(minorManaPotion, new RunicShopItem(10, "Coin", RunicShopGeneric.iconWithLore(minorManaPotion, 8)));
        shopItems.put(azanaShopArcherBow, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopArcherBow, 12)));
        shopItems.put(azanaShopClericMace, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopClericMace, 12)));
        shopItems.put(azanaShopMageStaff, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopMageStaff, 12)));
        shopItems.put(azanaShopRogueSword, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopRogueSword, 12)));
        shopItems.put(azanaShopWarriorAxe, new RunicShopItem(12, "Coin", RunicShopGeneric.iconWithLore(azanaShopWarriorAxe, 12)));
        shopItems.put(azanaShopArcherBoots, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopArcherBoots, 9)));
        shopItems.put(azanaShopClericHelmet, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopClericHelmet, 9)));
        shopItems.put(azanaShopMageHelmet, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopMageHelmet, 9)));
        shopItems.put(azanaShopRogueBoots, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopRogueBoots, 9)));
        shopItems.put(azanaShopWarriorHelmet, new RunicShopItem(9, "Coin", RunicShopGeneric.iconWithLore(azanaShopWarriorHelmet, 9)));
        return new RunicShopGeneric(45, ChatColor.YELLOW + "General Store", Collections.singletonList(102), shopItems, new int[]{0, 1, 2, 3, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22});
    }

    private static ItemStack resetSkillTreesIcon() {
        ItemStack infoItem = new ItemStack(Material.POPPED_CHORUS_FRUIT);
        ItemMeta meta = infoItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.LIGHT_PURPLE + "Reset Skill Trees");
        meta.setLore(ChatUtils.formattedText("&7Reset and refund your skill points!"));
        infoItem.setItemMeta(meta);
        return infoItem;
    }

    public RunicShopGeneric getRunicMage() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(resetSkillTreesIcon(), new RunicShopItem(0, "Coin", RunicShopGeneric.iconWithLore(resetSkillTreesIcon(), "Based on Level"), runRunicMageBuy()));
        return new RunicShopGeneric(9, ChatColor.LIGHT_PURPLE + "Runic Mage", Arrays.asList(131, 133, 134, 135, 136, 138, 139, 140, 141), shopItems);
    }

    private RunicItemRunnable runRunicMageBuy() {
        return player -> {
            // attempt to give player item (does not drop on floor)
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "resettree " + player.getName());
        };
    }

    private ItemStack wagonItem(TravelCMD.TravelType travelType, TravelCMD.TravelLocation travelLocation) {
        ItemStack wagonItem = new ItemStack(travelType.getMaterial());
        ItemMeta meta = wagonItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + "Fast Travel: " + travelLocation.getDisplay());
        meta.setLore(Arrays.asList("", ChatColor.GRAY + "Quickly travel to this destination!", ""));
        wagonItem.setItemMeta(meta);
        return wagonItem;

    }

    public RunicShopGeneric getCaptain() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.BOAT,
                                TravelCMD.TravelLocation.SUNS_REACH_CITADEL),
                        new RunicShopItem(120, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.SUNS_REACH_CITADEL), 120),
                                runFastTravelBuy(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.SUNS_REACH_CITADEL))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.BOAT,
                                TravelCMD.TravelLocation.BLACKGUARD_STRONGHOLD),
                        new RunicShopItem(120, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.BLACKGUARD_STRONGHOLD), 120),
                                runFastTravelBuy(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.BLACKGUARD_STRONGHOLD))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.BOAT,
                                TravelCMD.TravelLocation.CRIMSON_CHAPEL),
                        new RunicShopItem(120, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.CRIMSON_CHAPEL), 120),
                                runFastTravelBuy(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.CRIMSON_CHAPEL))
                );
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Captain", Arrays.asList(335, 328, 329, 330, 325, 336, 327), shopItems);
    }

    public RunicShopGeneric getWagonMaster() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.AZANA),
                        new RunicShopItem(15, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.AZANA), 15),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.AZANA))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.KOLDORE),
                        new RunicShopItem(15, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.KOLDORE), 15),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.KOLDORE))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.WHALETOWN),
                        new RunicShopItem(20, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WHALETOWN), 20),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WHALETOWN))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.HILSTEAD),
                        new RunicShopItem(20, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.HILSTEAD), 20),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.HILSTEAD))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.WINTERVALE),
                        new RunicShopItem(30, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WINTERVALE), 30),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WINTERVALE))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.DEAD_MANS_REST),
                        new RunicShopItem(30, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.DEAD_MANS_REST), 30),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.DEAD_MANS_REST))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.ISFODAR),
                        new RunicShopItem(45, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ISFODAR), 45),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ISFODAR))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.TIRNEAS),
                        new RunicShopItem(45, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.TIRNEAS), 45),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.TIRNEAS))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.ZENYTH),
                        new RunicShopItem(60, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ZENYTH), 60),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ZENYTH))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.NAHEEN),
                        new RunicShopItem(60, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAHEEN), 60),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAHEEN))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.NAZMORA),
                        new RunicShopItem(60, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAZMORA), 60),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAZMORA))
                );
        shopItems.put
                (
                        wagonItem(TravelCMD.TravelType.WAGON,
                                TravelCMD.TravelLocation.FROSTS_END),
                        new RunicShopItem(60, "Coin",
                                RunicShopGeneric.iconWithLore(wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.FROSTS_END), 60),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.FROSTS_END))
                );
        return new RunicShopGeneric(18, ChatColor.YELLOW + "Wagonmaster", Arrays.asList(245, 246, 249, 256, 262, 267, 333, 272, 334, 285, 315), shopItems);
    }

    private RunicItemRunnable runFastTravelBuy(TravelCMD.TravelType travelType, TravelCMD.TravelLocation travelLocation) {
        return player -> TravelCMD.fastTravelTask(player, travelType, travelLocation);
    }

    /*
    DUNGEON SHOPS
     */

    private static final int CAVERN_ARMOR_PRICE = 2;
    private static final String CAVERN_ITEM_CURRENCY = "HeadOfHexagonis";
    private final ItemStack crystalCavernArcherHelm = RunicItemsAPI.generateItemFromTemplate("crystal-cavern-archer-helm").generateItem();
    private final ItemStack crystalCavernArcherChest = RunicItemsAPI.generateItemFromTemplate("crystal-cavern-archer-chest").generateItem();
    private final ItemStack crystalCavernArcherLeggings = RunicItemsAPI.generateItemFromTemplate("crystal-cavern-archer-leggings").generateItem();
    private final ItemStack getCrystalCavernArcherBoots = RunicItemsAPI.generateItemFromTemplate("crystal-cavern-archer-boots").generateItem();

    public RunicShopGeneric getCavernShop() {
        LinkedHashMap<ItemStack, RunicShopItem> shopItems = new LinkedHashMap<>();
        shopItems.put(crystalCavernArcherHelm, new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, RunicShopGeneric.iconWithLore(crystalCavernArcherHelm, CAVERN_ARMOR_PRICE, "Head(s) of Hexagonis")));
        shopItems.put(crystalCavernArcherChest, new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, RunicShopGeneric.iconWithLore(crystalCavernArcherChest, CAVERN_ARMOR_PRICE, "Head(s) of Hexagonis")));
        shopItems.put(crystalCavernArcherLeggings, new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, RunicShopGeneric.iconWithLore(crystalCavernArcherLeggings, CAVERN_ARMOR_PRICE, "Head(s) of Hexagonis")));
        shopItems.put(getCrystalCavernArcherBoots, new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, RunicShopGeneric.iconWithLore(getCrystalCavernArcherBoots, CAVERN_ARMOR_PRICE, "Head(s) of Hexagonis")));
        return new RunicShopGeneric(36, ChatColor.YELLOW + "Crystal Cavern Shop", Collections.singletonList(52), shopItems, new int[]{0, 9, 18, 27});
    }
}
