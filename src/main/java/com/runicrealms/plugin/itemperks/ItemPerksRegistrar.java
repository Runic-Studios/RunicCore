package com.runicrealms.plugin.itemperks;

import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.Stat;

public class ItemPerksRegistrar {

    public ItemPerksRegistrar() {
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new GaleblessedPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new UndyingPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new RavenousPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new FrenzyPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new StoneSkinPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new AegisPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new GluttonyPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new MagicNullificationPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new TacticianPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new FortitudePerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new InfernalPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new WintersEdge2023Perk());
    }

    private void registerIgnarothPerks() {
        int[] amount = {20, 30, 40, 55, 80};
        for (Stat stat : Stat.values()) {
            for (int i = 1; i <= amount.length; i++) {
                RunicItemsAPI.getItemPerkManager().registerItemPerk(new IgnarothPerk("ignaroth-" + stat.getIdentifier() + "-" + i, stat, amount[i - 1]));
            }
        }
    }
}
