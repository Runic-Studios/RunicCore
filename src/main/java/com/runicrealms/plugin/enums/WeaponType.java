package com.runicrealms.plugin.enums;

import org.bukkit.inventory.ItemStack;

public enum WeaponType {

    BOW, MACE, STAFF, SWORD, AXE, GATHERING_TOOL, NONE;

    /**
     * Returns the type of held weapon. If none found, returns 'none'
     *
     * @param itemStack item in hand
     * @return type of runic weapon held
     */
    public static WeaponType matchType(final ItemStack itemStack) {
        if (itemStack == null) return null;
        switch (itemStack.getType()) {
            case BOW:
                return BOW;
            case WOODEN_SHOVEL:
            case GOLDEN_SHOVEL:
            case DIAMOND_SHOVEL:
                return MACE;
            case WOODEN_HOE:
            case GOLDEN_HOE:
            case DIAMOND_HOE:
                return STAFF;
            case WOODEN_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
                return SWORD;
            case WOODEN_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
                return AXE;
            case WOODEN_PICKAXE:
            case IRON_PICKAXE:
            case GOLDEN_PICKAXE:
            case DIAMOND_PICKAXE:
            case IRON_SHOVEL:
            case IRON_HOE:
            case IRON_SWORD:
            case IRON_AXE:
                return GATHERING_TOOL;
            default:
                return NONE;
        }
    }
}
