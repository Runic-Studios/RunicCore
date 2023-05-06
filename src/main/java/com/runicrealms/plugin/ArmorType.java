package com.runicrealms.plugin;

import org.bukkit.inventory.ItemStack;

public enum ArmorType {

    HELMET(5), CHESTPLATE(6), LEGGINGS(7), BOOTS(8), OFFHAND(40);

    private final int slot;

    ArmorType(int slot) {
        this.slot = slot;
    }

    /**
     * Returns the type of armor. If none found, returns 'none'
     *
     * @param itemStack item in hand
     * @return type of runic weapon held
     */
    public static ArmorType matchType(final ItemStack itemStack) {
        if (itemStack == null) return null;
        return switch (itemStack.getType()) {
            case CHAINMAIL_HELMET, GOLDEN_HELMET, DIAMOND_HELMET, LEATHER_HELMET, IRON_HELMET ->
                    HELMET;
            case CHAINMAIL_CHESTPLATE, GOLDEN_CHESTPLATE, DIAMOND_CHESTPLATE, LEATHER_CHESTPLATE, IRON_CHESTPLATE ->
                    CHESTPLATE;
            case CHAINMAIL_LEGGINGS, GOLDEN_LEGGINGS, DIAMOND_LEGGINGS, LEATHER_LEGGINGS, IRON_LEGGINGS ->
                    LEGGINGS;
            case CHAINMAIL_BOOTS, GOLDEN_BOOTS, DIAMOND_BOOTS, LEATHER_BOOTS, IRON_BOOTS -> BOOTS;
            default -> null;
        };
    }

    public int getSlot() {
        return slot;
    }
}
