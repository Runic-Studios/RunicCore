package com.runicrealms.plugin.parties;

import com.runicrealms.plugin.utilities.GUIItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum GroupPurpose {

    DUNGEON_SUNKEN_LIBRARY(GroupPurposeType.DUNGEON, GUIItem.dispItem(Material.IRON_BARS, "&eSunken Library &7Lvl 25, 3-5 players")),

    QUESTS_LEVEL_0_10(GroupPurposeType.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 0-10")),
    QUESTS_LEVEL_11_20(GroupPurposeType.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 11-20")),
    QUESTS_LEVEL_21_30(GroupPurposeType.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 21-30")),
    QUESTS_LEVEL_31_40(GroupPurposeType.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 31-40")),
    QUESTS_LEVEL_41_50(GroupPurposeType.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 41-50")),
    QUESTS_LEVEL_51_60(GroupPurposeType.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 51-60")),

    GRINDING_LEVEL_0_10(GroupPurposeType.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 0-10")),
    GRINDING_LEVEL_11_20(GroupPurposeType.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 11-20")),
    GRINDING_LEVEL_21_30(GroupPurposeType.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 21-30")),
    GRINDING_LEVEL_31_40(GroupPurposeType.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 31-40")),
    GRINDING_LEVEL_41_50(GroupPurposeType.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 41-50")),
    GRINDING_LEVEL_51_60(GroupPurposeType.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 51-60")),

    SPIDER_QUEEN(GroupPurposeType.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eMiniboss &7Lvl 5-10, Spider Queen"));

    private GroupPurposeType type;
    private ItemStack item;

    GroupPurpose(GroupPurposeType type, ItemStack item) {
        this.type = type;
        this.item = item;
    }

    public GroupPurposeType getType() {
        return this.type;
    }

    public ItemStack getDisplayName() {
        return this.item;
    }

    public enum GroupPurposeType {
        DUNGEON(GUIItem.dispItem(Material.IRON_BARS, ChatColor.YELLOW, "Dungeon", "Gather allies to fight powerful foes")),
        QUESTS(GUIItem.dispItem(Material.BOOK, ChatColor.YELLOW, "Quests", "Level up by questing with friends")),
        GRINDING(GUIItem.dispItem(Material.IRON_PICKAXE, ChatColor.YELLOW, "Mob Grinding", "Fight enemies and level up with friends")),
        MINIBOSS(GUIItem.dispItem(Material.IRON_SWORD, ChatColor.YELLOW, "Miniboss", "Defeat hard bosses with the help of some allies"));

        private ItemStack item;

        GroupPurposeType(ItemStack item) {
            this.item = item;
        }

        public ItemStack getItem() {
            return this.item;
        }
    }

}
