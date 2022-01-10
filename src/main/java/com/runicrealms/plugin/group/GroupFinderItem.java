package com.runicrealms.plugin.group;

import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public enum GroupFinderItem implements IGroupFinderItem {

    /*
    Dungeons
     */
    SEBATHS_CAVE(5, QueueReason.DUNGEONS, "Gritzgore",
            "Sebath’s Cave", "Silkwood Forest"),
    CRYSTAL_CAVERN(12, QueueReason.DUNGEONS, "a_storz",
            "Crystal Cavern", "Whaletown"),
    JORUNDRS_KEEP(15, QueueReason.DUNGEONS, "GoodUHCTipZAKO",
            "Jorundr’s Keep", "Hilstead"),
    SUNKEN_LIBRARY(25, QueueReason.DUNGEONS, "Haku",
            "Sunken Library", "Dead Man's Rest"),
    CRYPTS_OF_DERA(35, QueueReason.DUNGEONS, "Anubis",
            "Crypts of Dera", "Zenyth Desert"),
    THE_FROZEN_FORTRESS(60, QueueReason.DUNGEONS, "adaydremer",
            "&eThe Frozen Fortress", "Frost's End"),
    /*
    Grinding
     */
    ROOKIE_BANDITS(5, QueueReason.GRINDING, "Ramus", "Rookie Bandits", "Lawson’s Farm"),
    FOREST_SPIDERS(11, QueueReason.GRINDING, "Zombie_Slayer50", "Forest Spiders", "Silkwood Forest"),
    BARBARIANS(18, QueueReason.GRINDING, "henrik172", "Barbarians", "Ruins of Togrund"),
    AZANIAN_SOLDIERS(25, QueueReason.GRINDING, "ereizoi", "Azanian Soldiers", "Haunted Cliffs"),
    DESERT_HUSK(31, QueueReason.GRINDING, "husklover277", "Desert Husks", "Zenyth Desert"),
    HOBGOBLIN(49, QueueReason.GRINDING, "_ElGoblino", "Hobgoblin", "Orc Outpost"),
    INFERNAL_ARMY(60, QueueReason.GRINDING, "0mah", "Infernal Army", "Valmyra"),
    /*
    Mini-bosses
     */
    BLACK_RIDER(5, QueueReason.MINI_BOSSES, "TexHuK_", "Black Rider", "Silkwood Forest"),
    CAVE_MOTHER(5, QueueReason.MINI_BOSSES, "Zombie_Slayer50", "Cave Mother", "Silkwood Forest"),
    IRON_SOLDIER(8, QueueReason.MINI_BOSSES, "DoubleDark", "Iron Soldier", "Blackrail Burrow"),
    TOGRUND_THE_BLIGHTED(14, QueueReason.MINI_BOSSES, "adrix013", "Togrund the Blighted", "Ruins of Togrund"),
    PHARINDAR(20, QueueReason.MINI_BOSSES, "Elem", "Pharindar", "Wintervale Outskirts"),
    ADMIRAL_VEX(20, QueueReason.MINI_BOSSES, "3vilP4nd4", "Admiral Vex", "Dead Man’s Rest"),
    GOLEM_LORD(25, QueueReason.MINI_BOSSES, "Andreexd13", "Golem Lord", "Tireneas"),
    MASTER_FELDRUID(35, QueueReason.MINI_BOSSES, "Furion", "Master Feldruid", "Tireneas"),
    SUN_PRIEST(40, QueueReason.MINI_BOSSES, "Toadmare", "Sun Priest", "Zenyth"),
    PYROMANCER(55, QueueReason.MINI_BOSSES, "ChavezFam505", "Pyromancer", "Valmyra Citadel");

    private final int minLevel;
    private final QueueReason queueReason;
    private final String skullPlayerName;
    private final String menuItemName;
    private final String menuItemLocation;

    /**
     * Used to create the UI for dungeons in the group finder.
     *
     * @param minLevel         min level to queue for this menu item
     * @param queueReason      the sub-category of this menu item
     * @param skullPlayerName  name of the boss NPC player skin so its head can be used
     * @param menuItemName     display name of the item
     * @param menuItemLocation the location of the menu item's mob on the map
     */
    GroupFinderItem(int minLevel, QueueReason queueReason, String skullPlayerName, String menuItemName, String menuItemLocation) {
        this.minLevel = minLevel;
        this.queueReason = queueReason;
        this.skullPlayerName = skullPlayerName;
        this.menuItemName = ColorUtil.format(menuItemName);
        this.menuItemLocation = menuItemLocation;
    }

    @Override
    public int getMinLevel() {
        return minLevel;
    }

    @Override
    public QueueReason getQueueReason() {
        return queueReason;
    }

    @Override
    public String getSkullPlayerName() {
        return skullPlayerName;
    }

    @Override
    public String getMenuItemName() {
        return ChatColor.YELLOW + menuItemName;
    }

    @Override
    public String[] getMenuItemDescription() {
        return new String[]{"&7Req Lv &f" + this.minLevel, "&7Location &f" + this.menuItemLocation};
    }

    /**
     * Helper method to create skulls matching the name of the player
     *
     * @return an ItemStack with a skinned head to be used as a menu item
     */
    public ItemStack getMenuItem() {
        ItemStack item = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short) 3);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        assert skullMeta != null;
        skullMeta.setDisplayName(this.getMenuItemName());
        ArrayList<String> lore = new ArrayList<>();
        for (String s : this.getMenuItemDescription()) {
            lore.add(ColorUtil.format(s));
        }
        skullMeta.setLore(lore);
        if (!this.getSkullPlayerName().equals(""))
            skullMeta.setOwner(this.getSkullPlayerName());
        item.setItemMeta(skullMeta);
        return item;
    }
}
