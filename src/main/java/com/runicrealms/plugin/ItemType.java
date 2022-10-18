package com.runicrealms.plugin;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public enum ItemType {

    CLOTH, LEATHER, GILDED, MAIL, PLATE, GEMSTONE, MAINHAND, OFFHAND, CONSUMABLE, ARCHER, CLERIC, MAGE, ROGUE, WARRIOR, AIR;

    public static ItemType matchType(final ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        if (itemStack.getType() == Material.SHEARS) {
            switch (((Damageable) itemStack.getItemMeta()).getDamage()) {
                case 237:
                case 234:
                    return OFFHAND;
                case 25:
                    return PLATE; // (iron)
                case 20:
                    return GILDED; // (gold)
                case 15:
                    return MAIL; // (chain mail)
                case 10:
                    return LEATHER; // (leather)
                case 5:
                    return CLOTH; // (diamond)
                default:
                    return CLOTH;
            }
        } else {
            switch (itemStack.getType()) {
                case IRON_HELMET:
                case IRON_CHESTPLATE:
                case IRON_LEGGINGS:
                case IRON_BOOTS:
                    return PLATE;
                case GOLDEN_HELMET:
                case GOLDEN_CHESTPLATE:
                case GOLDEN_LEGGINGS:
                case GOLDEN_BOOTS:
                    return GILDED;
                case CHAINMAIL_HELMET:
                case CHAINMAIL_CHESTPLATE:
                case CHAINMAIL_LEGGINGS:
                case CHAINMAIL_BOOTS:
                    return MAIL;
                case LEATHER_HELMET:
                case LEATHER_CHESTPLATE:
                case LEATHER_LEGGINGS:
                case LEATHER_BOOTS:
                    return LEATHER;
                case DIAMOND_HELMET:
                case DIAMOND_CHESTPLATE:
                case DIAMOND_LEGGINGS:
                case DIAMOND_BOOTS:
                    return CLOTH;
                case REDSTONE:
                case LAPIS_LAZULI:
                case QUARTZ:
                case EMERALD:
                case DIAMOND:
                    return GEMSTONE;
                case IRON_SWORD:
                    return MAINHAND;
                case BOOK:
                case FEATHER:
                case FIRE_CHARGE:
                case RABBIT_FOOT:
                case SHIELD:
                case STONE_SHOVEL:
                case STONE_HOE:
                case STONE_SWORD:
                case STONE_AXE:
                    return OFFHAND;
                case FLINT:
                case PURPLE_DYE:
                    return CONSUMABLE;
                case BOW:
                    return ARCHER;
                case WOODEN_SHOVEL:
                    return CLERIC;
                case WOODEN_HOE:
                    return MAGE;
                case WOODEN_SWORD:
                    return ROGUE;
                case WOODEN_AXE:
                    return WARRIOR;
                default:
                    return AIR;
            }
        }
    }
}
