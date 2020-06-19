package com.runicrealms.plugin.group;

import com.runicrealms.plugin.utilities.GUIItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum GroupPurpose {

    DUNGEON_SUNKEN_LIBRARY(Type.DUNGEON, GUIItem.dispItem(Material.IRON_BARS, "&eSunken Library", new String[] {"&7• Level 25+", "&7• ~3 players recommended", "&7• Dead Man's Rest"}), 10),
    DUNGEON_CRYPTS_OF_DERA(Type.DUNGEON, GUIItem.dispItem(Material.IRON_BARS, "&eCrypts of Dera", new String[] {"&7• Level 35+", "&7• ~5 players recommended", "&7• Zenyth Desert"}), 12),
    DUNGEON_FROZEN_FORTRESS(Type.DUNGEON, GUIItem.dispItem(Material.IRON_BARS, "&eThe Frozen Fortress", new String[] {"&7• Level 60+", "&7• ~X players recommended", "&7• Frost's End"}), 20),

    QUESTS_LEVEL_0_10(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests", new String[] {"&7Level 0-10"}), 5),
    QUESTS_LEVEL_11_20(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests", new String[] {"&7Level 11-20"}), 5),
    QUESTS_LEVEL_21_30(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests", new String[] {"&7Level 21-30"}), 5),
    QUESTS_LEVEL_31_40(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests", new String[] {"&7Level 31-40"}), 5),
    QUESTS_LEVEL_41_50(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests", new String[] {"&7Level 41-50"}), 5),
    QUESTS_LEVEL_51_60(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests", new String[] {"&7Level 51-60"}),5),

    GRINDING_ROOKIE_BANDITS(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eRookie Bandits", new String[] {"&7• Level 5-10", "&7• Lawson's Farm"}), 5),
    GRINDING_FOREST_SPIDERS(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eForest Spiders", new String[] {"&7• Level 11-17", "&7• Silverwood Forest"}), 5),
    GRINDING_BARBARIANS(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eBarbarians", new String[] {"&7• Level 18-25", "&7• Ruins of Togrund"}), 5),
    GRINDING_AZANIAN_SOLDIERS(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eAzanian Soldiers", new String[] {"&7• Level 25-30", "&7• Haunted Cliffs"}), 5),
    GRINDING_DESERT_HUSKS(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eDesert Husks", new String[] {"&7• Level 31-48", "&7• Zenyth Desert"}), 5),
    GRINDING_HOBGOBLIN(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eHobgoblins", new String[] {"&7• Level 49-59", "&7• Orc Outpost"}), 5),
    GRINDING_INFERNAL_ARMY(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eInfernal Army", new String[] {"&7• Level 60+", "&7• Valmyra"}), 5),

    MINIBOSS_BLACK_RIDER(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eThe Black Rider", new String[] {"&7• Level 5+", "&7• Silverwood Camp"}), 3),
    MINIBOSS_CAVE_MOTHER(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eCave Mother", new String[] {"&7• Level 5+", "&7• Silverwood Caves"}), 3),
    MINIBOSS_IRON_SOLDIER(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eIron Soldier", new String[] {"&7• Level 8+", "&7• Koldorian Mines"}), 3),
    MINIBOSS_TOGRUND_THE_BLIGHTED(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eTogrund the Blighted", new String[] {"&7• Level 14+", "&7• Hilstead"}), 3),
    MINIBOSS_PHARINDAR(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&ePharindar", new String[] {"&7• Level 20+", "&7• Wintervale Outskirts"}), 3),
    MINIBOSS_ADMIRAL_VEX(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eAdmiral Vex", new String[] {"&7• Level 20+", "&7• Dead Man's Rest"}), 3),
    MINIBOSS_GOLEM_LORD(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eGolem Lord", new String[] {"&7• Level 25+", "&7• Tireneas"}), 3),
    MINIBOSS_MASTER_FELDRUID(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eMaster Feldruid", new String[] {"&7• Level 35+", "&7• Tireneas"}), 3),
    MINIBOSS_SUN_PRIEST(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eSun Priest", new String[] {"&7• Level 40+", "&7• Zenyth"}), 3),
    MINIBOSS_PYROMANCER(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&ePyromancer", new String[] {"&7• Level 55+", "&7• Valmyra Citadel"}), 3);

    private Type type;
    private ItemStack item;
    private int max;

    GroupPurpose(Type type, ItemStack item, int max) {
        this.type = type;
        this.item = item;
        this.max = max;
    }

    public Type getType() {
        return this.type;
    }

    public ItemStack getIcon() {
        return this.item;
    }

    public int getMaxMembers() {
        return this.max;
    }

    public enum Type {
        DUNGEON("Dungeon", GUIItem.dispItem(Material.IRON_BARS, ChatColor.YELLOW, "Dungeon", "Gather allies to fight powerful foes")),
        QUESTS("Quests", GUIItem.dispItem(Material.BOOK, ChatColor.YELLOW, "Quests", "Level up by questing with friends")),
        GRINDING("Grinding", GUIItem.dispItem(Material.IRON_PICKAXE, ChatColor.YELLOW, "Mob Grinding", "Fight enemies and level up with friends")),
        MINIBOSS("Miniboss", GUIItem.dispItem(Material.IRON_SWORD, ChatColor.YELLOW, "Miniboss", "Defeat hard bosses with the help of some allies"));

        private String name;
        private ItemStack item;

        Type(String name, ItemStack item) {
            this.name = name;
            this.item = item;
        }

        public String getName() {
            return this.name;
        }

        public ItemStack getIcon() {
            return this.item;
        }
    }

}
