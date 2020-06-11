package com.runicrealms.plugin.item;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.armor.TierSetEnum;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.enums.ItemTypeEnum;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import com.runicrealms.plugin.utilities.ColorUtil;
import com.runicrealms.plugin.utilities.NumRounder;
import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;

public class LoreGenerator {

    public static void generateHearthstoneLore(ItemStack hearthstone) {

        // grab our ItemMeta, ItemLore
        ItemMeta meta = hearthstone.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        meta.setDisplayName(ChatColor.AQUA + "Hearthstone");

        // grab our NBT attributes wrapper
        NBTItem nbti = new NBTItem(hearthstone);

        // item display
        String loc = nbti.getString("location");
        lore.add("");
//        lore.add(ChatColor.WHITE + "Left click: "
//                + ChatColor.GRAY + "Return to your "
//                + ChatColor.GOLD + "Guild Hall");
        lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + "RIGHT CLICK " + ChatColor.GREEN + loc);
        lore.add(ChatColor.GRAY + "Return to your hometown!");
        lore.add("");
        lore.add(ChatColor.GRAY + "Speak to an "
                + ChatColor.YELLOW + "innkeeper "
                + ChatColor.GRAY + "to change your home");
        if (AttributeUtil.getCustomString(hearthstone, "soulbound").equals("true")) {
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Soulbound");
        }
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        hearthstone.setItemMeta(meta);
    }

    /**
     *
     * @param item ItemStack to create
     * @param dispColor color of item corresponds to tier
     * @param dispName name of item
     * @param extra used for professions
     * @param reForge used for weapons
     * @param effectLore used for tiersets
     */
    public static void generateItemLore(ItemStack item, ChatColor dispColor, String dispName,
                                        String extra, boolean reForge, String effectLore) {

        // grab our material, ItemMeta, ItemLore
        ItemTypeEnum itemType = ItemTypeEnum.matchType(item);
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();
        meta.setDisplayName(dispColor + dispName);

        int socketCount = (int) AttributeUtil.getCustomDouble(item, "custom.socketCount");
        int currentSockets = (int) AttributeUtil.getCustomDouble(item, "custom.currentSockets");
        if (socketCount != 0) {
            lore.add(ChatColor.GRAY + "[" + currentSockets + "/" + socketCount + "] Gems");
        }

        int reqLv = (int) AttributeUtil.getCustomDouble(item, "required.level");
        if (reqLv != 0) {
            lore.add(ChatColor.DARK_GRAY + "Lv. Min: " + reqLv);
        }

        lore.add("");

        // -------------------------------------------------------------------------------------------
        // for weapons/gemstones/custom boosts
        int minDamage = (int) AttributeUtil.getCustomDouble(item, "custom.minDamage");
        int maxDamage = (int) AttributeUtil.getCustomDouble(item, "custom.maxDamage");
        int customHealth = (int) AttributeUtil.getCustomDouble(item, "custom.maxHealth");
        double customAttSpeed = AttributeUtil.getCustomDouble(item, "custom.attackSpeed");
        int manaBoost = (int) AttributeUtil.getCustomDouble(item, "custom.manaBoost");
        double damageBoost = AttributeUtil.getCustomDouble(item, "custom.attackDamage");
        double healingBoost = AttributeUtil.getCustomDouble(item, "custom.healingBoost");
        double magicBoost = AttributeUtil.getCustomDouble(item, "custom.magicDamage");
        double shieldAmt = AttributeUtil.getCustomDouble(item, "custom.shield");
        String spellStr = "";
        if (AttributeUtil.getSpell(item, "secondarySpell") != null)
            spellStr = AttributeUtil.getSpell(item, "secondarySpell");
        String sneakSpellStr = "";
        if (AttributeUtil.getSpell(item, "sneakSpell") != null)
            sneakSpellStr = AttributeUtil.getSpell(item, "sneakSpell");
        // -------------------------------------------------------------------------------------------

        // -------------------------------------------------------------------------------------------
        // enchants
        String location = AttributeUtil.getCustomString(item, "scroll.location");
        String enchantment = AttributeUtil.getCustomString(item, "scroll.enchantment");
        int enchantPercent = (int) AttributeUtil.getCustomDouble(item, "scroll.percent");
        if (!location.equals("")) {
            lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + "RIGHT CLICK" + " " + ChatColor.GREEN + location);
            lore.add(ChatColor.GRAY + "Teleport to a city!");
        }
        if (!enchantment.equals("")) {
            lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + "Enchantment" + " " + ChatColor.GREEN + enchantment + " " + enchantPercent + "%");
            for (String s : extra.split("\n")) {
                lore.add(ChatColor.GRAY + s);
            }
        }
        // -------------------------------------------------------------------------------------------

        if (minDamage != 0 && maxDamage != 0) {
            if (reForge) {
                lore.add(ChatColor.RED + "+ " + minDamage + "-" + maxDamage + "⚔ (Reforged)");
            } else {
                lore.add(ChatColor.RED + "+ " + minDamage + "-" + maxDamage + "⚔");
            }
        }
        if (customHealth != 0) {
            lore.add(ChatColor.RED + "+ " + customHealth + "❤");
        }
        if (customAttSpeed != 0) {
            double roundedSpeed = NumRounder.round(customAttSpeed);
            lore.add(ChatColor.RED + "+ " + roundedSpeed + " Att Speed");
        }
        if (manaBoost != 0) {
            lore.add(ChatColor.DARK_AQUA + "+ " + manaBoost + "✸");
        }
        if (damageBoost != 0) {
            lore.add(ChatColor.RED + "+ " + (int) damageBoost + "⚔");
        }
        if (healingBoost != 0) {
            lore.add(ChatColor.GREEN + "+ " + (int) healingBoost + "✦");
        }
        if (magicBoost != 0) {
            lore.add(ChatColor.DARK_AQUA + "+ " + (int) magicBoost + "ʔ");
        }
        if (shieldAmt != 0) {
            lore.add(ChatColor.WHITE + "+ " + (int) shieldAmt + "■");
        }

        lore.add("");

        if (!spellStr.equals("")) {
            try {
                Spell spell = RunicCore.getSpellManager().getSpellByName(spellStr);
                String command = "RIGHT CLICK";
                if (item.getType() == Material.BOW) command = "LEFT CLICK";
                lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + command + " " + ChatColor.GREEN + spell.getName());
                for (String s : spell.getDescription().split("\n")) {
                    lore.add(ChatColor.GRAY + s);
                }
                lore.add(ChatColor.DARK_AQUA + "Costs " + spell.getManaCost() + "✸");
                lore.add("");
            } catch (NullPointerException e) {
                RunicCore.getInstance().getLogger().info(" §4Error: spell not found... " + spellStr); // debug
            }
        }

        /*
        Artifact reforging
         */
        if (!sneakSpellStr.equals("")) {
            try {
                Spell spell = RunicCore.getSpellManager().getSpellByName(sneakSpellStr);
                String command = "SNEAK + RIGHT";
                if (item.getType() == Material.BOW) command = "SNEAK + LEFT";
                lore.add(ChatColor.GOLD + "" + ChatColor.BOLD + command + " " + ChatColor.GREEN + spell.getName());
                for (String s : spell.getDescription().split("\n")) {
                    lore.add(ChatColor.GRAY + s);
                }
                lore.add(ChatColor.DARK_AQUA + "Costs " + spell.getManaCost() + "✸");
                lore.add("");
            } catch (NullPointerException e) {
                RunicCore.getInstance().getLogger().info(" §4Error: spell not found... " + sneakSpellStr); // debug
            }
        }

        // add additional lore if necessary
//        if (!effectLore.equals("")) {
//            String[] extraEffectLore = effectLore.split("\n");
//            for (String s : extraEffectLore) {
//                lore.add(ColorUtil.format(s));
//            }
//        }
        String tierSet = AttributeUtil.getCustomString(item, "tierset");
        if (!tierSet.equals("")) {
            String tierSetLore = TierSetEnum.valueOf(tierSet.toUpperCase()).getEffectLore();
            String[] extraEffectLore = tierSetLore.split("\n");
            for (String s : extraEffectLore) {
                lore.add(ColorUtil.format(s));
            }
        }

            // add rarity
        if (dispColor == ChatColor.WHITE) {
            lore.add(ChatColor.WHITE + "Crafted");
        } else if (dispColor == ChatColor.GRAY) {
            lore.add(ChatColor.GRAY + "Common");
        } else if (dispColor == ChatColor.GREEN) {
            lore.add(ChatColor.GREEN + "Uncommon");
        } else if (dispColor == ChatColor.AQUA) {
            lore.add(ChatColor.AQUA + "Rare");
        } else if (dispColor == ChatColor.LIGHT_PURPLE) {
            lore.add(ChatColor.LIGHT_PURPLE + "Epic");
        } else if (dispColor == ChatColor.GOLD) {
            lore.add(ChatColor.GOLD + "Legendary");
        } else if (dispColor == ChatColor.YELLOW) {
            lore.add(ChatColor.YELLOW + "Unique");
        }

        // add type of item lore
        String type;
        switch (itemType) {
            case PLATE:
                type = "Plate"; // (iron)
                break;
            case GILDED:
                type = "Gilded"; // (gold)
                break;
            case MAIL:
                type = "Mail"; // (chainmail)
                break;
            case LEATHER:
                type = "Leather"; // (leather)
                break;
            case CLOTH:
                type = "Cloth"; // (diamond)
                break;
            case GEMSTONE:
                type = "Gemstone";
                break;
            case MAINHAND:
                type = "Main Hand";
                break;
            case OFFHAND:
                type = "Off-Hand";
                break;
            case CONSUMABLE:
                type = "Consumable";
                break;
            case ARCHER:
                type = "Bow";
                break;
            case CLERIC:
                type = "Mace";
                break;
            case MAGE:
                type = "Staff";
                break;
            case ROGUE:
                type = "Sword";
                break;
            case WARRIOR:
                type = "Axe";
                break;
            default:
                type = "Something went wrong";
                break;
        }
        lore.add(ChatColor.GRAY + type);

        if (AttributeUtil.getCustomString(item, "untradeable").equals("true")) {
            lore.add(ChatColor.DARK_GRAY + "Untradeable");
        }
        if (AttributeUtil.getCustomString(item, "soulbound").equals("true")) {
            lore.add(ChatColor.DARK_GRAY + "Soulbound");
        }

        if (!extra.equals("") && enchantment.equals("")) {
            String[] extraLore = extra.split("\n");
            for (String s : extraLore) {
                lore.add(ColorUtil.format(s));
            }
        } else if (!enchantment.equals("")) {
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Use this on an item");
        }

        // set other flags
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    // creates lore based on attributes
    private static void fillLore(ArrayList<String> lore, ItemStack item, String className) {
        double min = AttributeUtil.getCustomDouble(item, "custom.minDamage");
        double max = AttributeUtil.getCustomDouble(item, "custom.maxDamage");
        double speed;
        if (className.equals("Archer")) {
            speed = AttributeUtil.getCustomDouble(item, "custom.bowSpeed");
        } else {
            speed = AttributeUtil.getGenericDouble(item, "generic.attackSpeed");
        }
        double roundedSpeed = NumRounder.round(24+speed);
        lore.add(ChatColor.RED + "Att Speed: " + roundedSpeed);
        lore.add(ChatColor.RED + "DMG: " + (int) min + "-" + (int) max);
    }

    public static void generateGoldPouchLore(ItemStack goldPouch) {

        // grab our material, ItemMeta, ItemLore
        ItemMeta meta = goldPouch.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();

        int currentAmount = (int) AttributeUtil.getCustomDouble(goldPouch, "goldAmount");
        int pouchSize = (int) AttributeUtil.getCustomDouble(goldPouch, "pouchSize");

        String prefix = "";
        if (pouchSize < 256) {
            prefix = "Small ";
        } else if (pouchSize > 256) {
            prefix = "Large ";
        }

        meta.setDisplayName(ChatColor.GOLD + prefix + "Gold Pouch " + ChatColor.GREEN + ChatColor.BOLD + currentAmount + "c");
        lore.add(ChatColor.GRAY + "A pouch that holds " + ChatColor.WHITE + pouchSize + ChatColor.GRAY + " coins");

        // set other flags
        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);

        // update lore, meta
        meta.setLore(lore);
        ((Damageable) meta).setDamage(234);
        goldPouch.setItemMeta(meta);
    }
}
