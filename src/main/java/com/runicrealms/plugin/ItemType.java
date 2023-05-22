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
            return switch (((Damageable) itemStack.getItemMeta()).getDamage()) {
                case 235, 236, 237 -> OFFHAND;
                default -> CLOTH;
            };
        } else {
            return switch (itemStack.getType()) {
                case IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS -> PLATE;
                case GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS -> GILDED;
                case CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS ->
                        MAIL;
                case LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS -> LEATHER;
                case DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS -> CLOTH;
                case REDSTONE, LAPIS_LAZULI, QUARTZ, EMERALD, DIAMOND -> GEMSTONE;
                case IRON_SWORD -> MAINHAND;
                case BOOK, FEATHER, FIRE_CHARGE, RABBIT_FOOT, SHIELD, STONE_SHOVEL, STONE_HOE, STONE_SWORD, STONE_AXE, TRIDENT ->
                        OFFHAND;
                case FLINT, PURPLE_DYE -> CONSUMABLE;
                case BOW -> ARCHER;
                case WOODEN_SHOVEL -> CLERIC;
                case WOODEN_HOE -> MAGE;
                case WOODEN_SWORD -> ROGUE;
                case WOODEN_AXE -> WARRIOR;
                default -> AIR;
            };
        }
    }
}
