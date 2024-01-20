package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.SafeZoneLocation;
import com.runicrealms.plugin.TravelLocation;
import com.runicrealms.plugin.TravelType;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.common.util.Pair;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.stats.RunicItemTag;
import com.runicrealms.plugin.runicitems.item.template.RunicItemTemplate;
import com.runicrealms.plugin.runicitems.util.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class to create runic item shops with custom purchase runnables
 */
public class RunicItemShopHelper {
    private static final List<Pair<String, Integer>> SILVER_KEY_ITEM = new ArrayList<>();
    private static final List<Pair<String, Integer>> GOLD_KEY_ITEM = new ArrayList<>();
    private static final List<Pair<String, Integer>> ETHEREAL_KEY_ITEM = new ArrayList<>();
    private static final String SILVER_KEY = "silver-key";
    private static final String GOLD_KEY = "gold-key";
    private static final String ETHEREAL_KEY = "ethereal-key";
    private static final String GENERALS_KEY = "generals-key";
    private static final String KEEPERS_KEY = "keepers-key";
    private static final String WARDENS_KEY = "wardens-key";
    private static final String BOREALIS_KEY = "borealis-key";
    private static final String XALAKYTE_KEY = "xalakyte-key";

    private static final String[] KEYS = {SILVER_KEY, GOLD_KEY, ETHEREAL_KEY, GENERALS_KEY, KEEPERS_KEY, WARDENS_KEY, BOREALIS_KEY, XALAKYTE_KEY};

    static {
        SILVER_KEY_ITEM.add(Pair.pair(SILVER_KEY, 1));
        GOLD_KEY_ITEM.add(Pair.pair(GOLD_KEY, 1));
        ETHEREAL_KEY_ITEM.add(Pair.pair(ETHEREAL_KEY, 1));
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
    private final ItemStack stonehavenHearthstone = RunicItemsAPI.generateItemFromTemplate("hearthstone-stonehaven").generateItem();

    public RunicItemShopHelper() {
        /*
        UNIQUE
         */
        getCaptain();
        getNazmoraCaptain();
        getRunicMage();
        initializeInnkeepers();
        /*
        GATEKEEPERS
         */
        getCaveGatekeepers();
        getCavernGatekeepers();
        getJorundrsKeepGatekeepers();
        getLibraryGatekeepers();
        getCryptsGatekeepers();
        getFortressGatekeepers();
        getIgnarothLairGatekeepers();

        //temp
        this.getBrownSteedRefund();
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

    /**
     * Shorthand to create simple price maps for 1 item with 1 cost
     *
     * @param item templateID of price item
     * @return a map to pass to constructor
     */
    static List<Pair<String, Integer>> createReqItemMap(String item) {
        return Collections.singletonList(Pair.pair(item, 1));
    }

    private ItemStack boatItem(TravelType travelType, TravelLocation travelLocation) {
        ItemStack wagonItem = new ItemStack(travelType.getMaterial());
        ItemMeta meta = wagonItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + "Fast Travel: " + travelLocation.getDisplay());
        meta.setLore(Arrays.asList("", ChatColor.GRAY + "Quickly travel to this destination!"));
        wagonItem.setItemMeta(meta);
        return wagonItem;
    }

    public RunicShopGeneric getCaptain() {
        ArrayList<RunicShopItem> shopItems = new ArrayList<>();
//        shopItems.add
//                (
//                        new RunicShopItem(
//                                120,
//                                boatItem(TravelType.BOAT, TravelLocation.SUNS_REACH_CITADEL),
//                                runBoatBuy(TravelLocation.SUNS_REACH_CITADEL))
//                );
//        shopItems.add
//                (
//                        new RunicShopItem(
//                                120,
//                                boatItem(TravelType.BOAT, TravelLocation.BLACKGUARD_STRONGHOLD),
//                                runBoatBuy(TravelLocation.BLACKGUARD_STRONGHOLD))
//                );
        shopItems.add
                (
                        new RunicShopItem(
                                0,
                                boatItem(TravelType.BOAT, TravelLocation.CRIMSON_CHAPEL),
                                runBoatBuy(TravelLocation.CRIMSON_CHAPEL))
                );
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Captain", Arrays.asList(376, 328, 329, 330, 336, 327, 497), shopItems);
    }

    public Set<RunicShopGeneric> getCaveGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(515, SILVER_KEY_ITEM, DungeonLocation.SEBATHS_CAVE, 1);
        Gatekeeper second = new Gatekeeper(516, GOLD_KEY_ITEM, DungeonLocation.SEBATHS_CAVE, 2);
        gateKeepers.add(first);
        gateKeepers.add(second);
        return gateKeepers;
    }

    /*
    GATEKEEPERS
     */

    public Set<RunicShopGeneric> getCavernGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(517, ETHEREAL_KEY_ITEM, DungeonLocation.CRYSTAL_CAVERN, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getCryptsGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(522, SILVER_KEY_ITEM, DungeonLocation.CRYPTS_OF_DERA, 1);
        Gatekeeper second = new Gatekeeper(523, ETHEREAL_KEY_ITEM, DungeonLocation.CRYPTS_OF_DERA, 2);
        Gatekeeper third = new Gatekeeper(524, GOLD_KEY_ITEM, DungeonLocation.CRYPTS_OF_DERA, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getFortressGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(537, createReqItemMap("FrozenHeart"), DungeonLocation.FROZEN_FORTRESS, 0);
        Gatekeeper second = new Gatekeeper(525, SILVER_KEY_ITEM, DungeonLocation.FROZEN_FORTRESS, 1);
        Gatekeeper third = new Gatekeeper(526, createReqItemMap(XALAKYTE_KEY), DungeonLocation.FROZEN_FORTRESS, 2);
        Gatekeeper fourth = new Gatekeeper(527, createReqItemMap(BOREALIS_KEY), DungeonLocation.FROZEN_FORTRESS, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        gateKeepers.add(fourth);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getIgnarothLairGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(818, Arrays.asList(Pair.pair("paimon-vessel", 1), Pair.pair("cyrus-vessel", 1)), DungeonLocation.IGNAROTHS_LAIR, 1);
        Gatekeeper second = new Gatekeeper(831, Arrays.asList(Pair.pair("paimon-vessel", 1), Pair.pair("cyrus-vessel", 1), Pair.pair("crimson-flesh", 8)), DungeonLocation.IGNAROTHS_LAIR, 2);
        Gatekeeper third = new Gatekeeper(832, Arrays.asList(Pair.pair("paimon-vessel", 1), Pair.pair("cyrus-vessel", 1), Pair.pair("crimson-flesh", 50)), DungeonLocation.IGNAROTHS_LAIR, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        return gateKeepers;
    }

    public RunicShopGeneric getInnkeeper(String identifier, ItemStack hearthstone, int innkeeperId) {
        ArrayList<RunicShopItem> shopItems = new ArrayList<>();
        shopItems.add(new RunicShopItem(0, hearthstone, runHearthstoneChange(identifier)));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Innkeeper", Collections.singletonList(innkeeperId), shopItems);
    }

    public Set<RunicShopGeneric> getJorundrsKeepGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        List<Pair<String, Integer>> requiredItems = new ArrayList<>();
        requiredItems.add(Pair.pair(GENERALS_KEY, 1));
        requiredItems.add(Pair.pair(KEEPERS_KEY, 1));
        requiredItems.add(Pair.pair(WARDENS_KEY, 1));
        Gatekeeper first = new Gatekeeper(623, requiredItems, DungeonLocation.JORUNDRS_KEEP, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getLibraryGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        List<Pair<String, Integer>> requiredItems = new ArrayList<>();
        requiredItems.add(Pair.pair(SILVER_KEY, 1));
        requiredItems.add(Pair.pair(ETHEREAL_KEY, 1));
        requiredItems.add(Pair.pair(GOLD_KEY, 1));
        Gatekeeper first = new Gatekeeper(519, requiredItems, DungeonLocation.SUNKEN_LIBRARY, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public RunicShopGeneric getNazmoraCaptain() {
        ArrayList<RunicShopItem> shopItems = new ArrayList<>();
        shopItems.add
                (
                        new RunicShopItem(0,
                                boatItem(TravelType.BOAT, TravelLocation.NAZMORA),
                                runBoatBuy(TravelLocation.NAZMORA))
                );
        shopItems.add
                (
                        new RunicShopItem(0,
                                boatItem(TravelType.BOAT, TravelLocation.ORC_OUTPOST),
                                runBoatBuy(TravelLocation.ORC_OUTPOST))
                );
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Captain", Arrays.asList(325, 531), shopItems);
    }

    public RunicShopGeneric getRunicMage() {
        ArrayList<RunicShopItem> shopItems = new ArrayList<>();
        shopItems.add(new RunicShopItem(0, resetSkillTreesIcon(), runRunicMageBuy()));
        return new RunicShopGeneric(9, ChatColor.LIGHT_PURPLE + "Runic Mage", Arrays.asList(131, 133, 134, 135, 136, 138, 139, 140, 141, 734), shopItems);
    }

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
        getInnkeeper("naheen", naheenHearthstone, 400);
        getInnkeeper("nazmora", nazmoraHearthstone, 392);
        getInnkeeper("stonehaven", stonehavenHearthstone, 732);
    }

    private RunicItemRunnable runBoatBuy(TravelLocation travelLocation) {
        return player -> TravelLocation.fastTravelTask(player, TravelType.BOAT, travelLocation);
    }

    private RunicItemRunnable runHearthstoneChange(String location) {
        return player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            player.sendMessage(ChatColor.AQUA + "You have changed your hearthstone location to " + SafeZoneLocation.getFromIdentifier(location).getDisplay() + "!");
            player.getInventory().setItem(8, SafeZoneLocation.getFromIdentifier(location).getItemStack());
            player.closeInventory();
        };
    }

    private RunicItemRunnable runRunicMageBuy() {
        return player -> {
            // attempt to give player item (does not drop on floor)
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "resettree " + player.getName());
            player.closeInventory();
        };
    }

    private RunicShopGeneric getBrownSteedRefund() {
        ArrayList<RunicShopItem> shopItems = new ArrayList<>();
        ItemStack coin = RunicItemsAPI.generateItemFromTemplate("coin").generateGUIItem();
        coin.setAmount(64);
        ItemMeta meta = coin.getItemMeta();
        meta.setDisplayName(ColorUtil.format("&e1250 Coins"));
        meta.setLore(Collections.singletonList(ColorUtil.format("&c&lClear your inventory before clicking this!")));
        coin.setItemMeta(meta);

        shopItems.add(new RunicShopItem(Collections.singletonList(Pair.pair("brown-steed", 1)), coin, player -> {
            for (int i = 0; i < 1250; i++) {
                RunicItemsAPI.addItem(player.getInventory(), CurrencyUtil.goldCoin(), true);
            }
        }));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Refund Vendor", Collections.singletonList(816), shopItems);
    }

    /**
     * A hardcoded method to check if an item is the template id of a dungeon key
     *
     * @param templateId the id of the item
     * @return if an item is the template id of a dungeon key
     */
    public static boolean isDungeonKey(@Nullable String templateId) {
        for (String key : KEYS) {
            if (key.equals(templateId)) {
                return true;
            }
        }

        return false;
    }

    /**
     * A method that removes dungeon keys from a player
     *
     * @param player the player
     */
    public static void clearDungeonItems(@NotNull Player player) {
        boolean removedItem = false;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getAmount() <= 0 || item.getType() == Material.AIR) {
                continue;
            }

            RunicItemTemplate template = RunicItemsAPI.getItemStackTemplate(item);

            if (template != null && (RunicItemShopHelper.isDungeonKey(template.getId()) || template.getTags().contains(RunicItemTag.DUNGEON_ITEM))) {
                item.setAmount(0);
                removedItem = true;
            }
        }

        if (removedItem) {
            player.sendMessage(ChatColor.GRAY + "Your dungeon items have been removed.");
        }
    }
}
