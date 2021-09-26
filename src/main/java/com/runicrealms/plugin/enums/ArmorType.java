package com.runicrealms.plugin.enums;

import org.bukkit.inventory.ItemStack;

public enum ArmorType {

    HELMET(5), CHESTPLATE(6), LEGGINGS(7), BOOTS(8), OFFHAND(40);

    private final int slot;

    ArmorType(int slot) {
        this.slot = slot;
    }

    /**
     * Returns the type of armor. If none found, returns 'none'
     * @param itemStack item in hand
     * @return type of runic weapon held
     */
    public static ArmorType matchType(final ItemStack itemStack) {
        if (itemStack == null) return null;
        switch (itemStack.getType()) {
            case CHAINMAIL_HELMET:
            case GOLDEN_HELMET:
            case DIAMOND_HELMET:
            case LEATHER_HELMET:
            case IRON_HELMET:
                return HELMET;
            case CHAINMAIL_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case DIAMOND_CHESTPLATE:
            case LEATHER_CHESTPLATE:
            case IRON_CHESTPLATE:
                return CHESTPLATE;
            case CHAINMAIL_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case DIAMOND_LEGGINGS:
            case LEATHER_LEGGINGS:
            case IRON_LEGGINGS:
                return LEGGINGS;
            case CHAINMAIL_BOOTS:
            case GOLDEN_BOOTS:
            case DIAMOND_BOOTS:
            case LEATHER_BOOTS:
            case IRON_BOOTS:
                return BOOTS;
            case BOOK:
            case FIRE_CHARGE:
            case RABBIT_FOOT:
            case SHIELD:
            case STONE_SHOVEL:
            case STONE_HOE:
            case STONE_SWORD:
            case STONE_AXE:
                return OFFHAND;
            default:
                return null;
        }
    }

    public int getSlot() {
        return slot;
    }
}
