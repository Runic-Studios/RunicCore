package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;

public class ItemPerksRegistrar {

    public ItemPerksRegistrar() {
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new GaleblessedPerk());
    }

}
