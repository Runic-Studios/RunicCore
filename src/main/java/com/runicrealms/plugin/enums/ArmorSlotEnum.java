package com.runicrealms.plugin.enums;

import org.bukkit.inventory.ItemStack;

public enum ArmorSlotEnum {
    HELMET(5), CHESTPLATE(6), LEGGINGS(7), BOOTS(8), OFFHAND(9);

    private final int slot;

    ArmorSlotEnum(int slot){
        this.slot = slot;
    }

    /**
     * Attempts to match the ArmorType for the specified ItemStack.
     *
     * @param itemStack The ItemStack to parse the skilltypes of.
     * @return The parsed ArmorType. (null if none were found.)
     */
    public static ArmorSlotEnum matchSlot(final ItemStack itemStack){
        if(itemStack == null) { return null; }
        switch (itemStack.getType()){
            case DIAMOND_HELMET:
            case GOLDEN_HELMET:
            case IRON_HELMET:
            case CHAINMAIL_HELMET:
            case LEATHER_HELMET:
            case PUMPKIN:
            case SHEARS:
            case LEGACY_SKULL_ITEM:
                return HELMET;
            case DIAMOND_CHESTPLATE:
            case GOLDEN_CHESTPLATE:
            case IRON_CHESTPLATE:
            case CHAINMAIL_CHESTPLATE:
            case LEATHER_CHESTPLATE:
                return CHESTPLATE;
            case DIAMOND_LEGGINGS:
            case GOLDEN_LEGGINGS:
            case IRON_LEGGINGS:
            case CHAINMAIL_LEGGINGS:
            case LEATHER_LEGGINGS:
                return LEGGINGS;
            case DIAMOND_BOOTS:
            case GOLDEN_BOOTS:
            case IRON_BOOTS:
            case CHAINMAIL_BOOTS:
            case LEATHER_BOOTS:
                return BOOTS;
            default:
                return OFFHAND;
        }
    }

    public int getSlot(){
        return slot;
    }
}