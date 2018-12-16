package us.fortherealm.plugin.utilities;

import org.bukkit.inventory.ItemStack;

public enum WeaponEnum {

    BOW, MACE, STAFF, SWORD, AXE;

    public static WeaponEnum matchType(final ItemStack itemStack){
        if(itemStack == null) { return null; }
        switch (itemStack.getType()){
            case BOW:
                return BOW;
            case WOODEN_SHOVEL:
            case STONE_SHOVEL:
            case IRON_SHOVEL:
            case GOLDEN_SHOVEL:
            case DIAMOND_SHOVEL:
                return MACE;
            case WOODEN_HOE:
            case STONE_HOE:
            case IRON_HOE:
            case GOLDEN_HOE:
            case DIAMOND_HOE:
                return STAFF;
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLDEN_SWORD:
            case DIAMOND_SWORD:
                return SWORD;
            case WOODEN_AXE:
            case STONE_AXE:
            case IRON_AXE:
            case GOLDEN_AXE:
            case DIAMOND_AXE:
                return AXE;
            default:
                return null;
        }
    }
}
