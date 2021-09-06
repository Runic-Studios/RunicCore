package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.commands.TravelCMD;
import com.runicrealms.plugin.item.hearthstone.HearthstoneLocation;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

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
        getMountVendor();
        getRunicMage();
        getTailor();
        getWagonMaster();
        initializeInnkeepers();
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
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(2, "Coin", bottle));
        shopItems.add(new RunicShopItem(8, "Coin", minorHealingPotion));
        shopItems.add(new RunicShopItem(8, "Coin", minorManaPotion));
        shopItems.add(new RunicShopItem(16, "Coin", majorHealingPotion));
        shopItems.add(new RunicShopItem(16, "Coin", majorManaPotion));
        shopItems.add(new RunicShopItem(24, "Coin", greaterHealingPotion));
        shopItems.add(new RunicShopItem(24, "Coin", greaterManaPotion));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Alchemist", Arrays.asList(101, 103, 104, 105, 106, 107, 108, 109, 110, 111), shopItems);
    }

    private final ItemStack goldPouch = RunicItemsAPI.generateItemFromTemplate("gold-pouch").generateItem();

    public RunicShopGeneric getBagVendor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(128, "Coin", goldPouch));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Bag Vendor", Arrays.asList(179, 181, 182, 183, 185, 186, 187, 189, 190, 192, 194), shopItems);
    }

    private final ItemStack bread = RunicItemsAPI.generateItemFromTemplate("Bread").generateItem();

    public RunicShopGeneric getBaker() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(4, "Coin", bread));
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
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(2, "Coin", beetroot));
        shopItems.add(new RunicShopItem(3, "Coin", carrot));
        shopItems.add(new RunicShopItem(10, "Coin", minorHealingPotion));
        shopItems.add(new RunicShopItem(10, "Coin", minorManaPotion));
        shopItems.add(new RunicShopItem(12, "Coin", azanaShopArcherBow));
        shopItems.add(new RunicShopItem(12, "Coin", azanaShopClericMace));
        shopItems.add(new RunicShopItem(12, "Coin", azanaShopMageStaff));
        shopItems.add(new RunicShopItem(12, "Coin", azanaShopRogueSword));
        shopItems.add(new RunicShopItem(12, "Coin", azanaShopWarriorAxe));
        shopItems.add(new RunicShopItem(9, "Coin", azanaShopArcherBoots));
        shopItems.add(new RunicShopItem(9, "Coin", azanaShopClericHelmet));
        shopItems.add(new RunicShopItem(9, "Coin", azanaShopMageHelmet));
        shopItems.add(new RunicShopItem(9, "Coin", azanaShopRogueBoots));
        shopItems.add(new RunicShopItem(9, "Coin", azanaShopWarriorHelmet));
        return new RunicShopGeneric(45, ChatColor.YELLOW + "General Store", Collections.singletonList(102), shopItems,
                new int[]{0, 1, 2, 3, 9, 10, 11, 12, 13, 18, 19, 20, 21, 22});
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
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(0, "Coin", resetSkillTreesIcon(), "Based on Level", runRunicMageBuy()));
        return new RunicShopGeneric(9, ChatColor.LIGHT_PURPLE + "Runic Mage", Arrays.asList(131, 133, 134, 135, 136, 138, 139, 140, 141), shopItems);
    }

    private RunicItemRunnable runRunicMageBuy() {
        return player -> {
            // attempt to give player item (does not drop on floor)
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "resettree " + player.getName());
        };
    }

    private final ItemStack thread = RunicItemsAPI.generateItemFromTemplate("Thread").generateItem();

    public RunicShopGeneric getTailor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(6, "Coin", thread));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Tailor", Arrays.asList(233, 237, 247, 259, 283, 509, 314), shopItems);
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
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add
                (
                        new RunicShopItem(120, "Coin",
                                wagonItem(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.SUNS_REACH_CITADEL),
                                runFastTravelBuy(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.SUNS_REACH_CITADEL))
                );
        shopItems.add
                (
                        new RunicShopItem(120, "Coin",
                                wagonItem(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.BLACKGUARD_STRONGHOLD),
                                runFastTravelBuy(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.BLACKGUARD_STRONGHOLD))
                );
        shopItems.add
                (
                        new RunicShopItem(120, "Coin",
                                wagonItem(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.CRIMSON_CHAPEL),
                                runFastTravelBuy(TravelCMD.TravelType.BOAT, TravelCMD.TravelLocation.CRIMSON_CHAPEL))
                );
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Captain", Arrays.asList(376, 328, 329, 330, 325, 336, 327), shopItems);
    }

    public RunicShopGeneric getWagonMaster() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add
                (
                        new RunicShopItem(15, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.AZANA),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.AZANA))
                );
        shopItems.add
                (
                        new RunicShopItem(15, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.KOLDORE),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.KOLDORE))
                );
        shopItems.add
                (
                        new RunicShopItem(20, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WHALETOWN),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WHALETOWN))
                );
        shopItems.add
                (
                        new RunicShopItem(20, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.HILSTEAD),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.HILSTEAD))
                );
        shopItems.add
                (
                        new RunicShopItem(30, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WINTERVALE),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.WINTERVALE))
                );
        shopItems.add
                (
                        new RunicShopItem(30, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.DEAD_MANS_REST),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.DEAD_MANS_REST))
                );
        shopItems.add
                (
                        new RunicShopItem(45, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ISFODAR),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ISFODAR))
                );
        shopItems.add
                (
                        new RunicShopItem(45, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.TIRNEAS),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.TIRNEAS))
                );
        shopItems.add
                (
                        new RunicShopItem(60, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ZENYTH),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.ZENYTH))
                );
        shopItems.add
                (
                        new RunicShopItem(60, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAHEEN),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAHEEN))
                );
        shopItems.add
                (
                        new RunicShopItem(60, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAZMORA),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.NAZMORA))
                );
        shopItems.add
                (
                        new RunicShopItem(60, "Coin",
                                wagonItem(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.FROSTS_END),
                                runFastTravelBuy(TravelCMD.TravelType.WAGON, TravelCMD.TravelLocation.FROSTS_END))
                );
        return new RunicShopGeneric(18, ChatColor.YELLOW + "Wagonmaster", Arrays.asList(245, 246, 249, 256, 262, 267, 333, 272, 334, 285, 315, 337), shopItems);
    }

    private RunicItemRunnable runFastTravelBuy(TravelCMD.TravelType travelType, TravelCMD.TravelLocation travelLocation) {
        return player -> TravelCMD.fastTravelTask(player, travelType, travelLocation);
    }

    private final ItemStack brownSteed = RunicItemsAPI.generateItemFromTemplate("brown-steed").generateItem();
    private final ItemStack chestnutMare = RunicItemsAPI.generateItemFromTemplate("chestnut-mare").generateItem();
    private final ItemStack grayStallion = RunicItemsAPI.generateItemFromTemplate("gray-stallion").generateItem();

    public RunicShopGeneric getMountVendor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(2000, "Coin", brownSteed));
        shopItems.add(new RunicShopItem(2000, "Coin", chestnutMare));
        shopItems.add(new RunicShopItem(2000, "Coin", grayStallion));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Mount Vendor", Arrays.asList(510, 239, 243, 257, 535, 534, 274, 508, 280, 284, 317), shopItems);
    }

    /*
    INNKEEPERS
     */
    private final ItemStack azanaHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-azana").generateItem();
    private final ItemStack koldoreHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-koldore").generateItem();
    private final ItemStack whaletownHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-whaletown").generateItem();
    private final ItemStack hilsteadHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-hilstead").generateItem();
    private final ItemStack wintervaleHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-wintervale").generateItem();
    private final ItemStack dawnshireInnHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-dawnshire-inn").generateItem();
    private final ItemStack deadMansRestHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-dead-mans-rest").generateItem();
    private final ItemStack isfodarHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-isfodar").generateItem();
    private final ItemStack tireneasHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-tireneas").generateItem();
    private final ItemStack zenythHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-zenyth").generateItem();
    private final ItemStack naheenHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-naheen").generateItem();
    private final ItemStack nazmoraHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-nazmora").generateItem();

    public void initializeInnkeepers() {
        getInnkeeper("azana", azanaHearthstone, 393);
        getInnkeeper("koldore", koldoreHearthstone, 395);
        getInnkeeper("whaletown", whaletownHearthstone, 384);
        getInnkeeper("hilstead", hilsteadHearthstone, 394);
        getInnkeeper("wintervale", wintervaleHearthstone, 433);
        getInnkeeper("dawnshire_inn", dawnshireInnHearthstone, 570);
        getInnkeeper("dead_mans_rest", deadMansRestHearthstone, 397);
        getInnkeeper("isfodar", isfodarHearthstone, 388);
        getInnkeeper("tireneas", tireneasHearthstone, 399);
        getInnkeeper("zenyth", zenythHearthstone, 390);
        getInnkeeper("naheen", naheenHearthstone, 452);
        getInnkeeper("nazmora", nazmoraHearthstone, 392);
    }

    public RunicShopGeneric getInnkeeper(String identifier, ItemStack hearthstone, int innkeeperId) {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(0, "Coin", hearthstone, runHearthstoneChange(identifier)));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Innkeeper", Collections.singletonList(innkeeperId), shopItems);
    }

    private RunicItemRunnable runHearthstoneChange(String location) {
        return player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            player.sendMessage(ChatColor.AQUA + "You have changed your hearthstone location to " + HearthstoneLocation.getFromIdentifier(location).getDisplay() + "!");
            player.getInventory().setItem(8, HearthstoneLocation.getFromIdentifier(location).getItemStack());
        };
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
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, crystalCavernArcherHelm, "Head(s) of Hexagonis"));
        shopItems.add(new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, crystalCavernArcherChest, "Head(s) of Hexagonis"));
        shopItems.add(new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, crystalCavernArcherLeggings, "Head(s) of Hexagonis"));
        shopItems.add(new RunicShopItem(CAVERN_ARMOR_PRICE, CAVERN_ITEM_CURRENCY, getCrystalCavernArcherBoots, "Head(s) of Hexagonis"));
        return new RunicShopGeneric(36, ChatColor.YELLOW + "Crystal Cavern Shop", Collections.singletonList(52), shopItems, new int[]{0, 9, 18, 27});
    }
}
