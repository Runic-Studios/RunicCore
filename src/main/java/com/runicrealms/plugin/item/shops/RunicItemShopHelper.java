package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.CityLocation;
import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.TravelLocation;
import com.runicrealms.plugin.TravelType;
import com.runicrealms.plugin.professions.Profession;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

/**
 * Helper class to create runic item shops with custom purchase runnables
 */
public class RunicItemShopHelper {

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
        getRunicMage();
        initializeInnkeepers();
        /*
        PROFESSION TUTORS
         */
        getAlchemistTutor();
        getBlacksmithTutor();
        getEnchanterTutor();
        getHunterTutor();
        getJewelerTutor();
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

    private static ItemStack professionTutorIcon(Profession profession, Material material) {
        ItemStack infoItem = new ItemStack(material);
        ItemMeta meta = infoItem.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "LEARN PROFESSION - " + profession.getName());
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Learn this profession!");
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&e" + profession.getDescription()));
        lore.add("");
        lore.addAll(ChatUtils.formattedText("&cWarning: This will RESET your current profession!"));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        infoItem.setItemMeta(meta);
        return infoItem;
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

    public RunicShopGeneric getAlchemistTutor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<RunicShopItem>() {{
            add(new RunicShopItem(0, "Coin", professionTutorIcon(Profession.ALCHEMIST, Material.GLASS_BOTTLE), runProfessionTutorBuy(Profession.ALCHEMIST)));
        }};
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Alchemist Tutor", Collections.singletonList(225), shopItems);
    }

    public RunicShopGeneric getBlacksmithTutor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<RunicShopItem>() {{
            add(new RunicShopItem(0, "Coin", professionTutorIcon(Profession.BLACKSMITH, Material.IRON_CHESTPLATE), runProfessionTutorBuy(Profession.BLACKSMITH)));
        }};
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Blacksmith Tutor", Collections.singletonList(226), shopItems);
    }

    public RunicShopGeneric getCaptain() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add
                (
                        new RunicShopItem(120, "Coin",
                                boatItem(TravelType.BOAT, TravelLocation.SUNS_REACH_CITADEL),
                                runBoatBuy(TravelLocation.SUNS_REACH_CITADEL))
                );
        shopItems.add
                (
                        new RunicShopItem(120, "Coin",
                                boatItem(TravelType.BOAT, TravelLocation.BLACKGUARD_STRONGHOLD),
                                runBoatBuy(TravelLocation.BLACKGUARD_STRONGHOLD))
                );
        shopItems.add
                (
                        new RunicShopItem(120, "Coin",
                                boatItem(TravelType.BOAT, TravelLocation.CRIMSON_CHAPEL),
                                runBoatBuy(TravelLocation.CRIMSON_CHAPEL))
                );
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Captain", Arrays.asList(376, 328, 329, 330, 325, 336, 327), shopItems);
    }

    // todo: needs to accept multiple keys
    public Set<RunicShopGeneric> getCaveGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(515, "SilverKey", 1, DungeonLocation.SEBATHS_CAVE, 1);
        Gatekeeper second = new Gatekeeper(516, "GoldKey", 1, DungeonLocation.SEBATHS_CAVE, 2);
        gateKeepers.add(first);
        gateKeepers.add(second);
        return gateKeepers;
    }

    /*
    GATEKEEPERS
     */

    public Set<RunicShopGeneric> getCavernGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(517, "SilverKey", 1, DungeonLocation.CRYSTAL_CAVERN, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getCryptsGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(522, "SilverKey", 1, DungeonLocation.CRYPTS_OF_DERA, 1);
        Gatekeeper second = new Gatekeeper(523, "EtherealKey", 1, DungeonLocation.CRYPTS_OF_DERA, 2);
        Gatekeeper third = new Gatekeeper(524, "GoldKey", 1, DungeonLocation.CRYPTS_OF_DERA, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        return gateKeepers;
    }

    public RunicShopGeneric getCryptsShop() {
        RunicDungeonShop caveShop = new RunicDungeonShop
                (
                        2,
                        2,
                        DungeonLocation.CRYPTS_OF_DERA.getCurrencyTemplateId(),
                        new String[]{"triumph", "gilded-impaler", "prophets-cane", "nightshade", "sandfury"},
                        "crypts");
        return caveShop.buildRunicShopGeneric(45, ChatColor.YELLOW + "Crypts of Dera Shop", Collections.singletonList(35));
    }

    public RunicShopGeneric getEnchanterTutor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<RunicShopItem>() {{
            add(new RunicShopItem(0, "Coin", professionTutorIcon(Profession.ENCHANTER, Material.PURPLE_DYE), runProfessionTutorBuy(Profession.ENCHANTER)));
        }};
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Enchanter Tutor", Collections.singletonList(228), shopItems);
    }

    public Set<RunicShopGeneric> getFortressGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(537, "FrozenHeart", 1, DungeonLocation.FROZEN_FORTRESS, 0);
        Gatekeeper second = new Gatekeeper(525, "SilverKey", 1, DungeonLocation.FROZEN_FORTRESS, 1);
        Gatekeeper third = new Gatekeeper(526, "XalakyteKey", 1, DungeonLocation.FROZEN_FORTRESS, 2);
        Gatekeeper fourth = new Gatekeeper(527, "BorealisKey", 1, DungeonLocation.FROZEN_FORTRESS, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        gateKeepers.add(fourth);
        return gateKeepers;
    }

    public RunicShopGeneric getHunterTutor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<RunicShopItem>() {{
            add(new RunicShopItem(0, "Coin", professionTutorIcon(Profession.HUNTER, Material.LEATHER), runProfessionTutorBuy(Profession.HUNTER)));
        }};
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Hunter Tutor", Collections.singletonList(222), shopItems);
    }

    public RunicShopGeneric getInnkeeper(String identifier, ItemStack hearthstone, int innkeeperId) {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(0, "Coin", hearthstone, runHearthstoneChange(identifier)));
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Innkeeper", Collections.singletonList(innkeeperId), shopItems);
    }

    public RunicShopGeneric getJewelerTutor() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<RunicShopItem>() {{
            add(new RunicShopItem(0, "Coin", professionTutorIcon(Profession.JEWELER, Material.REDSTONE), runProfessionTutorBuy(Profession.JEWELER)));
        }};
        return new RunicShopGeneric(9, ChatColor.YELLOW + "Jeweler Tutor", Collections.singletonList(230), shopItems);
    }

    public Set<RunicShopGeneric> getJorundrsKeepGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(623, "GeneralsKey", 1, DungeonLocation.JORUNDRS_KEEP, 1);
        gateKeepers.add(first);
        return gateKeepers;
    }

    public Set<RunicShopGeneric> getLibraryGatekeepers() {
        Set<RunicShopGeneric> gateKeepers = new HashSet<>();
        Gatekeeper first = new Gatekeeper(519, "SilverKey", 1, DungeonLocation.SUNKEN_LIBRARY, 1);
        Gatekeeper second = new Gatekeeper(520, "EtherealKey", 1, DungeonLocation.SUNKEN_LIBRARY, 2);
        Gatekeeper third = new Gatekeeper(521, "GoldKey", 1, DungeonLocation.SUNKEN_LIBRARY, 3);
        gateKeepers.add(first);
        gateKeepers.add(second);
        gateKeepers.add(third);
        return gateKeepers;
    }

    public RunicShopGeneric getRunicMage() {
        LinkedHashSet<RunicShopItem> shopItems = new LinkedHashSet<>();
        shopItems.add(new RunicShopItem(0, "Coin", resetSkillTreesIcon(), "Based on Level", runRunicMageBuy()));
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

    private RunicItemRunnable runProfessionTutorBuy(Profession profession) {
        return player -> Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "setprof " + player.getName() + " " + profession.getName() + " true");
    }

    private RunicItemRunnable runRunicMageBuy() {
        return player -> {
            // attempt to give player item (does not drop on floor)
            Bukkit.dispatchCommand(Bukkit.getServer().getConsoleSender(), "resettree " + player.getName());
        };
    }
}
