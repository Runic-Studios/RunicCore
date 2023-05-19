package com.runicrealms.plugin.character;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.classes.CharacterClass;
import com.runicrealms.plugin.classes.SubClass;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
    private static final Map<CharacterClass, ItemStack> CLASS_ICONS = new HashMap<>();

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
        List<String> archerLore = new ArrayList<>();
        archerLore.add(ChatColor.GRAY + "");
        archerLore.add(ChatColor.GOLD + "● Long-range");
        archerLore.add(ChatColor.GOLD + "● Bowman");
        archerLore.add(ChatColor.GOLD + "● Single Target");
        archerLore.add(ChatColor.GRAY + "");
        archerLore.addAll
                (
                        ChatUtils.formattedText(ChatColor.GRAY + "The archer features a diverse array of damage, " +
                                "mobility, and utility spells, a master of natural terrain and single combat!")
                );
        archerLore.add("");
        archerLore.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Subclasses: ");
        StringBuilder stringBuilder = new StringBuilder();
        SubClass.ARCHER_SUBCLASSES.forEach(subClass -> {
            stringBuilder.setLength(0);
            stringBuilder
                    .append(ChatColor.AQUA)
                    .append("● ")
                    .append(subClass.getName());
            archerLore.add(stringBuilder.toString());
        });
        archerMeta.setLore(archerLore);
        archerItem.setItemMeta(archerMeta);
        CLASS_ICONS.put(CharacterClass.ARCHER, archerItem);

        ItemStack clericItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.cleric.material")), 1, CLERIC_ITEM_DURABILITY);
        ItemMeta clericMeta = clericItem.getItemMeta();
        assert clericMeta != null;
        clericMeta.setUnbreakable(true);
        clericMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        clericMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        clericMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Cleric ✦");


        List<String> clericLore = new ArrayList<>();
        clericLore.add(ChatColor.GRAY + "");
        clericLore.add(ChatColor.GOLD + "● Close-range");
        clericLore.add(ChatColor.GOLD + "● Healer");
        clericLore.add(ChatColor.GOLD + "● Area-of-effect");
        clericLore.add(ChatColor.GRAY + "");
        clericLore.addAll
                (
                        ChatUtils.formattedText(ChatColor.GRAY + "The cleric enjoys a range of crowd control, " +
                                "healing, and utility spells, bolstering any party!")
                );
        clericLore.add("");
        clericLore.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Subclasses: ");
        SubClass.CLERIC_SUBCLASSES.forEach(subClass -> {
            stringBuilder.setLength(0);
            stringBuilder
                    .append(ChatColor.AQUA)
                    .append("● ")
                    .append(subClass.getName());
            clericLore.add(stringBuilder.toString());
        });
        clericMeta.setLore(clericLore);
        clericItem.setItemMeta(clericMeta);
        CLASS_ICONS.put(CharacterClass.CLERIC, clericItem);

        ItemStack mageItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.mage.material")), 1, MAGE_ITEM_DURABILITY);
        ItemMeta mageMeta = mageItem.getItemMeta();
        assert mageMeta != null;
        mageMeta.setUnbreakable(true);
        mageMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        mageMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        mageMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Mage ʔ");
        List<String> mageLore = new ArrayList<>();
        mageLore.add(ChatColor.GRAY + "");
        mageLore.add(ChatColor.GOLD + "● Medium-range");
        mageLore.add(ChatColor.GOLD + "● Caster");
        mageLore.add(ChatColor.GOLD + "● Area-of-effect");
        mageLore.add(ChatColor.GRAY + "");
        mageLore.addAll
                (
                        ChatUtils.formattedText(ChatColor.GRAY + "The mage is a master of widespread damage, " +
                                "controlling the flow of battle and deadly if left unchecked in the back lines!")
                );
        mageLore.add("");
        mageLore.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Subclasses: ");
        SubClass.MAGE_SUBCLASSES.forEach(subClass -> {
            stringBuilder.setLength(0);
            stringBuilder
                    .append(ChatColor.AQUA)
                    .append("● ")
                    .append(subClass.getName());
            mageLore.add(stringBuilder.toString());
        });
        mageMeta.setLore(mageLore);
        mageItem.setItemMeta(mageMeta);
        CLASS_ICONS.put(CharacterClass.MAGE, mageItem);

        ItemStack rogueItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.rogue.material")), 1, ROGUE_ITEM_DURABILITY);
        ItemMeta rogueMeta = rogueItem.getItemMeta();
        assert rogueMeta != null;
        rogueMeta.setUnbreakable(true);
        rogueMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        rogueMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        rogueMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Rogue ⚔");
        List<String> rogueLore = new ArrayList<>();
        rogueLore.add(ChatColor.GRAY + "");
        rogueLore.add(ChatColor.GOLD + "● Close-range");
        rogueLore.add(ChatColor.GOLD + "● Fighter");
        rogueLore.add(ChatColor.GOLD + "● Single Target");
        rogueLore.add(ChatColor.GRAY + "");
        rogueLore.addAll
                (
                        ChatUtils.formattedText(ChatColor.GRAY + "The rogue does not play fair, arming itself " +
                                "with a set of stealth and mobility to engage any foe!")
                );
        rogueLore.add("");
        rogueLore.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Subclasses: ");
        SubClass.ROGUE_SUBCLASSES.forEach(subClass -> {
            stringBuilder.setLength(0);
            stringBuilder
                    .append(ChatColor.AQUA)
                    .append("● ")
                    .append(subClass.getName());
            rogueLore.add(stringBuilder.toString());
        });
        rogueMeta.setLore(rogueLore);
        rogueItem.setItemMeta(rogueMeta);
        CLASS_ICONS.put(CharacterClass.ROGUE, rogueItem);

        ItemStack warriorItem = new ItemStack(Material.getMaterial(RunicCore.getInstance().getConfig().getString("class-icons.warrior.material")), 1, WARRIOR_ITEM_DURABILITY);
        ItemMeta warriorMeta = warriorItem.getItemMeta();
        assert warriorMeta != null;
        warriorMeta.setUnbreakable(true);
        warriorMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        warriorMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        warriorMeta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Warrior ■");

        List<String> warriorLore = new ArrayList<>();
        warriorLore.add(ChatColor.GRAY + "");
        warriorLore.add(ChatColor.GOLD + "● Close-range");
        warriorLore.add(ChatColor.GOLD + "● Tank");
        warriorLore.add(ChatColor.GOLD + "● Single Target");
        warriorLore.add(ChatColor.GRAY + "");
        warriorLore.addAll
                (
                        ChatUtils.formattedText(ChatColor.GRAY + "The warrior is a force to be reckoned with, " +
                                "featuring both offensive and defensive spells to charge into the front lines!")
                );
        warriorLore.add("");
        warriorLore.add(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Subclasses: ");
        SubClass.WARRIOR_SUBCLASSES.forEach(subClass -> {
            stringBuilder.setLength(0);
            stringBuilder
                    .append(ChatColor.AQUA)
                    .append("● ")
                    .append(subClass.getName());
            warriorLore.add(stringBuilder.toString());
        });
        warriorMeta.setLore(warriorLore);
        warriorItem.setItemMeta(warriorMeta);
        CLASS_ICONS.put(CharacterClass.WARRIOR, warriorItem);

        EXIT_GAME_ITEM = new ItemStack(Material.OAK_DOOR, 1);
        ItemMeta exitGameItemMeta = EXIT_GAME_ITEM.getItemMeta();
        assert exitGameItemMeta != null;
        exitGameItemMeta.setDisplayName(ColorUtil.format("&r&cLeave the Realm"));
        EXIT_GAME_ITEM.setItemMeta(exitGameItemMeta);
    }

    private static void foo() {

    }

    public static Map<CharacterClass, ItemStack> getClassIcons() {
        return CLASS_ICONS;
    }
}
