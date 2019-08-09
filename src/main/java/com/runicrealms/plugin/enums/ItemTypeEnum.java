package com.runicrealms.plugin.enums;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public enum ItemTypeEnum {

    CLOTH, LEATHER, GILDED, MAIL, PLATE, GEMSTONE, OFFHAND, AIR;

    public static ItemTypeEnum matchType(final ItemStack itemStack){
        if(itemStack == null) { return null; }
        if (itemStack.getType() == Material.SHEARS) {
            switch (((Damageable) itemStack.getItemMeta()).getDamage()) {
                case 25:
                    return PLATE; // (iron)
                case 20:
                    return GILDED; // (gold)
                case 15:
                    return MAIL; // (chainmail)
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
                case BOOK:
                case FIRE_CHARGE:
                case FLINT:
                case RABBIT_FOOT:
                case SHIELD:
                case IRON_SWORD:
                    return OFFHAND;
                default:
                    return AIR;
            }
        }
    }
}
