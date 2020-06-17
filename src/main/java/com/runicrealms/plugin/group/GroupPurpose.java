package com.runicrealms.plugin.group;

import com.runicrealms.plugin.utilities.GUIItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum GroupPurpose {

    DUNGEON_SUNKEN_LIBRARY(Type.DUNGEON, GUIItem.dispItem(Material.IRON_BARS, "&eSunken Library &7Lvl 25, 3-5 players"), 10),

    QUESTS_LEVEL_0_10(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 0-10"), 5),
    QUESTS_LEVEL_11_20(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 11-20"), 5),
    QUESTS_LEVEL_21_30(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 21-30"), 5),
    QUESTS_LEVEL_31_40(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 31-40"), 5),
    QUESTS_LEVEL_41_50(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 41-50"), 5),
    QUESTS_LEVEL_51_60(Type.QUESTS, GUIItem.dispItem(Material.BOOK, "&eQuests &7Lvl 51-60"),5),

    GRINDING_LEVEL_0_10(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 0-10"), 5),
    GRINDING_LEVEL_11_20(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 11-20"), 5),
    GRINDING_LEVEL_21_30(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 21-30"), 5),
    GRINDING_LEVEL_31_40(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 31-40"), 5),
    GRINDING_LEVEL_41_50(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 41-50"), 5),
    GRINDING_LEVEL_51_60(Type.GRINDING, GUIItem.dispItem(Material.IRON_PICKAXE, "&eGrinding &7Lvl 51-60"), 5),

    SPIDER_QUEEN(Type.MINIBOSS, GUIItem.dispItem(Material.IRON_SWORD, "&eMiniboss &7Lvl 5-10, Spider Queen"), 3);

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
        DUNGEON(GUIItem.dispItem(Material.IRON_BARS, ChatColor.YELLOW, "Dungeon", "Gather allies to fight powerful foes")),
        QUESTS(GUIItem.dispItem(Material.BOOK, ChatColor.YELLOW, "Quests", "Level up by questing with friends")),
        GRINDING(GUIItem.dispItem(Material.IRON_PICKAXE, ChatColor.YELLOW, "Mob Grinding", "Fight enemies and level up with friends")),
        MINIBOSS(GUIItem.dispItem(Material.IRON_SWORD, ChatColor.YELLOW, "Miniboss", "Defeat hard bosses with the help of some allies"));

        private ItemStack item;

        Type(ItemStack item) {
            this.item = item;
        }

        public ItemStack getIcon() {
            return this.item;
        }
    }

}
