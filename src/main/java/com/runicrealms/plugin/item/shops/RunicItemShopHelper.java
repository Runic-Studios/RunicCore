package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.TravelLocation;
import com.runicrealms.plugin.TravelType;
import com.runicrealms.plugin.common.util.ChatUtils;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Helper class to create runic item shops with custom purchase runnables
 */
public class RunicItemShopHelper {

    private static final Map<String, Integer> SILVER_KEY_MAP = new HashMap<String, Integer>() {{
        put("SilverKey", 1);
    }};
    private static final Map<String, Integer> GOLD_KEY_MAP = new HashMap<String, Integer>() {{
        put("GoldKey", 1);
    }};
    private static final Map<String, Integer> ETHEREAL_KEY_MAP = new HashMap<String, Integer>() {{
        put("EtherealKey", 1);
    }};
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
    static Map<String, Integer> createReqItemMap(String item) {
        return new HashMap<String, Integer>() {{
            put(item, 1);
        }};
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
        Map<String, Integer> coin = new HashMap<String, Integer>() {{
            put("coin", 120);
        }};
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add
                (
                        new RunicShopItem(coin,
                                boatItem(TravelType.BOAT, TravelLocation.SUNS_REACH_CITADEL),
                                runBoatBuy(TravelLocation.SUNS_REACH_CITADEL))
                );
        shopItems.add
                (
                        new RunicShopItem(coin,
                                boatItem(TravelType.BOAT, TravelLocation.BLACKGUARD_STRONGHOLD),
                                runBoatBuy(TravelLocation.BLACKGUARD_STRONGHOLD))
                );
        shopItems.add
                (
                        new RunicShopItem(coin,
                                boatItem(TravelType.BOAT, TravelLocation.CRIMSON_CHAPEL),
                                runBoatBuy(TravelLocation.CRIMSON_CHAPEL))
                );
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Captain", Arrays.asList(376, 328, 329, 330, 336, 327, 497), shopItems);
    }

    public Set<RunicShopGeneric> getCaveGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(515, SILVER_KEY_MAP, DungeonLocation.SEBATHS_CAVE, 1);
        Gatekeeper second = new Gatekeeper(516, GOLD_KEY_MAP, DungeonLocation.SEBATHS_CAVE, 2);
        gateKeepers.add(first);
        gateKeepers.add(second);
        return gateKeepers;
    }

    /*
    GATEKEEPERS
     */

    public Set<RunicShopGeneric> getCavernGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Map<String, Integer> requiredItems = new HashMap<String, Integer>() {{
            put("SilverKey", 1);
            put("EtherealKey", 1);
            put("GoldKey", 1);
        }};
        Gatekeeper first = new Gatekeeper(517, requiredItems, DungeonLocation.CRYSTAL_CAVERN, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getCryptsGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(522, SILVER_KEY_MAP, DungeonLocation.CRYPTS_OF_DERA, 1);
        Gatekeeper second = new Gatekeeper(523, ETHEREAL_KEY_MAP, DungeonLocation.CRYPTS_OF_DERA, 2);
        Gatekeeper third = new Gatekeeper(524, GOLD_KEY_MAP, DungeonLocation.CRYPTS_OF_DERA, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getFortressGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(537, createReqItemMap("FrozenHeart"), DungeonLocation.FROZEN_FORTRESS, 0);
        Gatekeeper second = new Gatekeeper(525, SILVER_KEY_MAP, DungeonLocation.FROZEN_FORTRESS, 1);
        Gatekeeper third = new Gatekeeper(526, createReqItemMap("XalakyteKey"), DungeonLocation.FROZEN_FORTRESS, 2);
        Gatekeeper fourth = new Gatekeeper(527, createReqItemMap("BorealisKey"), DungeonLocation.FROZEN_FORTRESS, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        gateKeepers.add(fourth);
        return gateKeepers;
    }

    public RunicShopGeneric getInnkeeper(String identifier, ItemStack hearthstone, int innkeeperId) {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        Map<String, Integer> coin = new HashMap<String, Integer>() {{
            put("coin", 0);
        }};
        shopItems.add(new RunicShopItem(coin, hearthstone, runHearthstoneChange(identifier)));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Innkeeper", Collections.singletonList(innkeeperId), shopItems);
    }

    public Set<RunicShopGeneric> getJorundrsKeepGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Map<String, Integer> requiredItems = new HashMap<String, Integer>() {{
            put("GeneralsKey", 1);
            put("KeepersKey", 1);
            put("WardensKey", 1);
        }};
        Gatekeeper first = new Gatekeeper(623, requiredItems, DungeonLocation.JORUNDRS_KEEP, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getLibraryGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Map<String, Integer> requiredItems = new HashMap<String, Integer>() {{
            put("SilverKey", 1);
            put("EtherealKey", 1);
            put("GoldKey", 1);
        }};
        Gatekeeper first = new Gatekeeper(519, requiredItems, DungeonLocation.SUNKEN_LIBRARY, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public RunicShopGeneric getNazmoraCaptain() {
        Map<String, Integer> free = new HashMap<String, Integer>() {{
            put("coin", 0);
        }};
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add
                (
                        new RunicShopItem(free,
                                boatItem(TravelType.BOAT, TravelLocation.NAZMORA),
                                runBoatBuy(TravelLocation.NAZMORA))
                );
        shopItems.add
                (
                        new RunicShopItem(free,
                                boatItem(TravelType.BOAT, TravelLocation.ORC_OUTPOST),
                                runBoatBuy(TravelLocation.ORC_OUTPOST))
                );
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Captain", Arrays.asList(325, 531), shopItems);
    }

    public RunicShopGeneric getRunicMage() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        Map<String, Integer> coin = new HashMap<String, Integer>() {{
            put("coin", 0);
        }};
        shopItems.add(new RunicShopItem(coin, resetSkillTreesIcon(), runRunicMageBuy()));
        return new RunicShopGeneric(9, ChatColor.LIGHT_PURPLE + "Runic Mage", Arrays.asList(131, 133, 134, 135, 136, 138, 139, 140, 141), shopItems);
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
    }

    private RunicItemRunnable runBoatBuy(TravelLocation travelLocation) {
        return player -> TravelLocation.fastTravelTask(player, TravelType.BOAT, travelLocation);
    }

    private RunicItemRunnable runHearthstoneChange(String location) {
        return player -> {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.0f);
            player.sendMessage(ChatColor.AQUA + "You have changed your hearthstone location to " + CityLocation.getFromIdentifier(location).getDisplay() + "!");
            player.getInventory().setItem(8, CityLocation.getFromIdentifier(location).getItemStack());
        };
    }

    private RunicItemRunnable runRunicMageBuy() {
        return player -> {
            // attempt to give player item (does not drop on floor)
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "resettree " + player.getName());
        };
    }
}
