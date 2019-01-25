package us.fortherealm.plugin.enums;

import org.bukkit.inventory.ItemStack;

public enum ItemTypeEnum {

    CLOTH, LEATHER, MAIL, PLATE, CRYSTAL, GEMSTONE, AIR;

    public static ItemTypeEnum matchType(final ItemStack itemStack){
        if(itemStack == null) { return null; }
        switch (itemStack.getType()){
            case DIAMOND_HELMET:
            case DIAMOND_CHESTPLATE:
            case DIAMOND_LEGGINGS:
            case DIAMOND_BOOTS:
                return CRYSTAL;
            case IRON_HELMET:
            case IRON_CHESTPLATE:
            case IRON_LEGGINGS:
            case IRON_BOOTS:
                return PLATE;
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
            case GOLDEN_HELMET:
            case GOLDEN_CHESTPLATE:
            case GOLDEN_LEGGINGS:
            case GOLDEN_BOOTS:
                return CLOTH;
            case EMERALD:
            case REDSTONE:
            case LAPIS_LAZULI:
            case QUARTZ:
                return GEMSTONE;
            default:
                return AIR;
        }
    }
}
