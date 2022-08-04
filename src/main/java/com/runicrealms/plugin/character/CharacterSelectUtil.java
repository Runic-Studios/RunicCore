package com.runicrealms.plugin.character;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.ClassEnum;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CharacterSelectUtil {

    private static final FileConfiguration FILE_CONFIGURATION = RunicCore.getInstance().getConfig();
    private static final short ARCHER_ITEM_DURABILITY = (short) FILE_CONFIGURATION.getInt("class-icons.archer.damage");
    private static final short CLERIC_ITEM_DURABILITY = (short) FILE_CONFIGURATION.getInt("class-icons.cleric.damage");
    private static final short MAGE_ITEM_DURABILITY = (short) FILE_CONFIGURATION.getInt("class-icons.mage.damage");
    private static final short ROGUE_ITEM_DURABILITY = (short) FILE_CONFIGURATION.getInt("class-icons.rogue.damage");
    private static final short WARRIOR_ITEM_DURABILITY = (short) FILE_CONFIGURATION.getInt("class-icons.warrior.damage");

    public static final ItemStack CHARACTER_CREATE_ITEM;
    public static final ItemStack ONLY_KNIGHT_CREATE_ITEM;
    public static final ItemStack ONLY_CHAMPION_CREATE_ITEM;
    public static final ItemStack GO_BACK_ITEM;
    public static final ItemStack CONFIRM_DELETION_ITEM;
    public static final ItemStack EXIT_GAME_ITEM;
    private static final Map<ClassEnum, ItemStack> CLASS_ICONS = new HashMap<>();

    static {

        CHARACTER_CREATE_ITEM = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE, 1);
        ItemMeta creationMeta = CHARACTER_CREATE_ITEM.getItemMeta();
        assert creationMeta != null;
        creationMeta.setUnbreakable(true);
        creationMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        creationMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Create a Class");
        CHARACTER_CREATE_ITEM.setItemMeta(creationMeta);

        ONLY_KNIGHT_CREATE_ITEM = new ItemStack(Material.BARRIER, 1);
        ItemMeta knightMeta = ONLY_KNIGHT_CREATE_ITEM.getItemMeta();
        assert knightMeta != null;
        knightMeta.setUnbreakable(true);
        knightMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        knightMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You need " + ChatColor.AQUA + "Knight" + ChatColor.RED + " rank to use this slot");
        ONLY_KNIGHT_CREATE_ITEM.setItemMeta(knightMeta);

        ONLY_CHAMPION_CREATE_ITEM = new ItemStack(Material.BARRIER, 1);
        ItemMeta championMeta = ONLY_CHAMPION_CREATE_ITEM.getItemMeta();
        assert championMeta != null;
        championMeta.setUnbreakable(true);
        championMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        championMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "You need " + ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "Champion" + ChatColor.RED + "" + ChatColor.BOLD + " rank to use this slot");
        ONLY_CHAMPION_CREATE_ITEM.setItemMeta(championMeta);

        GO_BACK_ITEM = new ItemStack(Material.BARRIER);
        ItemMeta goBackMeta = GO_BACK_ITEM.getItemMeta();
        assert goBackMeta != null;
        goBackMeta.setUnbreakable(true);
        goBackMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        goBackMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Cancel");
        GO_BACK_ITEM.setItemMeta(goBackMeta);

        CONFIRM_DELETION_ITEM = new ItemStack(Material.SLIME_BALL);
        ItemMeta confirmDeletionMeta = CONFIRM_DELETION_ITEM.getItemMeta();
        assert confirmDeletionMeta != null;
        confirmDeletionMeta.setUnbreakable(true);
        confirmDeletionMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        confirmDeletionMeta.setDisplayName(ChatColor.RED + "" + ChatColor.BOLD + "Confirm Deletion");
        confirmDeletionMeta.setLore(Collections.singletonList(ChatColor.DARK_RED + "WARNING: There is no going back!"));
        CONFIRM_DELETION_ITEM.setItemMeta(confirmDeletionMeta);

        ItemStack archerItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.archer.material")), 1, ARCHER_ITEM_DURABILITY);
        ItemMeta archerMeta = archerItem.getItemMeta();
        assert archerMeta != null;
        archerMeta.setUnbreakable(true);
        archerMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        archerMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Archer ⚔");
        archerMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Long-range",
                ChatColor.GOLD + "● Bowman",
                ChatColor.GOLD + "● Single Target",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The archer features a diverse",
                ChatColor.GRAY + "pool of damage, mobility, and",
                ChatColor.GRAY + "utility spells, a master of",
                ChatColor.GRAY + "terrain and single combat!"
        ));
        archerItem.setItemMeta(archerMeta);
        CLASS_ICONS.put(ClassEnum.ARCHER, archerItem);

        ItemStack clericItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.cleric.material")), 1, CLERIC_ITEM_DURABILITY);
        ItemMeta clericMeta = clericItem.getItemMeta();
        assert clericMeta != null;
        clericMeta.setUnbreakable(true);
        clericMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        clericMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        clericMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Cleric ✦");
        clericMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Close-range",
                ChatColor.GOLD + "● Healer",
                ChatColor.GOLD + "● Area-of-effect",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The cleric features a range",
                ChatColor.GRAY + "crowd control, healing, and",
                ChatColor.GRAY + "utility spells, bolstering",
                ChatColor.GRAY + "any party!"
        ));
        clericItem.setItemMeta(clericMeta);
        CLASS_ICONS.put(ClassEnum.CLERIC, clericItem);

        ItemStack mageItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.mage.material")), 1, MAGE_ITEM_DURABILITY);
        ItemMeta mageMeta = mageItem.getItemMeta();
        assert mageMeta != null;
        mageMeta.setUnbreakable(true);
        mageMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        mageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        mageMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Mage ʔ");
        mageMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Medium-range",
                ChatColor.GOLD + "● Caster",
                ChatColor.GOLD + "● Area-of-effect",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The mage is a master of widespread",
                ChatColor.GRAY + "damage, controlling the flow of",
                ChatColor.GRAY + "battle and deadly if left unchecked",
                ChatColor.GRAY + "in the back lines!"
        ));
        mageItem.setItemMeta(mageMeta);
        CLASS_ICONS.put(ClassEnum.MAGE, mageItem);

        ItemStack rogueItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.rogue.material")), 1, ROGUE_ITEM_DURABILITY);
        ItemMeta rogueMeta = rogueItem.getItemMeta();
        assert rogueMeta != null;
        rogueMeta.setUnbreakable(true);
        rogueMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        rogueMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        rogueMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Rogue ⚔");
        rogueMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Close-range",
                ChatColor.GOLD + "● Duelist",
                ChatColor.GOLD + "● Single Target",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The rogue does not play fair,",
                ChatColor.GRAY + "Equipped with a pool of crowd",
                ChatColor.GRAY + "control, stealth, and damage",
                ChatColor.GRAY + "to engage any foe!"
        ));
        rogueItem.setItemMeta(rogueMeta);
        CLASS_ICONS.put(ClassEnum.ROGUE, rogueItem);

        ItemStack warriorItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.warrior.material")), 1, WARRIOR_ITEM_DURABILITY);
        ItemMeta warriorMeta = warriorItem.getItemMeta();
        assert warriorMeta != null;
        warriorMeta.setUnbreakable(true);
        warriorMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        warriorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        warriorMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Warrior ■");
        warriorMeta.setLore(Arrays.asList(
                ChatColor.GRAY + "",
                ChatColor.GOLD + "● Close-range",
                ChatColor.GOLD + "● Tank",
                ChatColor.GOLD + "● Single Target",
                ChatColor.GRAY + "",
                ChatColor.GRAY + "The warrior is a force to be",
                ChatColor.GRAY + "reckoned with, featuring both",
                ChatColor.GRAY + "offensive and defensive spells",
                ChatColor.GRAY + "to charge into the front lines!"
        ));
        warriorItem.setItemMeta(warriorMeta);
        CLASS_ICONS.put(ClassEnum.WARRIOR, warriorItem);

        EXIT_GAME_ITEM = new ItemStack(Material.OAK_DOOR, 1);
        ItemMeta exitGameItemMeta = EXIT_GAME_ITEM.getItemMeta();
        assert exitGameItemMeta != null;
        exitGameItemMeta.setDisplayName(ColorUtil.format("&r&cLeave the Realm"));
        EXIT_GAME_ITEM.setItemMeta(exitGameItemMeta);
    }

    public static Map<ClassEnum, ItemStack> getClassIcons() {
        return CLASS_ICONS;
    }
}
