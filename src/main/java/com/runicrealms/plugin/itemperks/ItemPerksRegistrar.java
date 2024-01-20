package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;

public class ItemPerksRegistrar {

    public ItemPerksRegistrar() {
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new GaleblessedPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new UndyingPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new RavenousPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new FrenzyPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new StoneSkinPerk());
    }

}
