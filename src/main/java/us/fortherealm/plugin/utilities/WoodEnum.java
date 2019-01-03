package us.fortherealm.plugin.utilities;

import org.bukkit.inventory.ItemStack;

public enum WoodEnum {

    LOGS;

    public static WoodEnum matchType(final ItemStack itemStack){
        if(itemStack == null) { return null; }
        switch (itemStack.getType()){
            case ACACIA_LOG:
            case BIRCH_LOG:
            case DARK_OAK_LOG:
            case JUNGLE_LOG:
            case OAK_LOG:
            case SPRUCE_LOG:
                return LOGS;
            default:
                return null;
        }
    }
}
