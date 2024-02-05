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
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new BloodlustPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new MagicNullificationPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new TacticianPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new FortitudePerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new InfernalPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new WintersEdge2023Perk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new PurityPerk());
        this.registerIgnarothPerks();
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new MechanizationPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new SelflessPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new ManawellPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new FocusPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new PraetoriumRougeWeaponPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new PraetoriumArcherWeaponPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new PraetoriumMageWeaponPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new PraetoriumWarriorWeaponPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new PraetoriumClericWeaponPerk());
        RunicItemsAPI.getItemPerkManager().registerItemPerk(new PraetoriumArmorPerk());
    }

    /**
     * A method that handles the 25 item perks for the offhand items in ignaroth's lair
     */
    private void registerIgnarothPerks() {
        RunicItemsAPI.getDynamicItemHandler().registerTextPlaceholder(new IgnarothPerk.InsideLairTextPlaceholder());
 
        int[] amount = {20, 30, 40, 55};
        for (Stat stat : Stat.values()) {
            for (int i = 1; i <= amount.length; i++) {
                RunicItemsAPI.getItemPerkManager().registerItemPerk(new IgnarothPerk("ignaroth-" + stat.getIdentifier() + "-" + i, stat, amount[i - 1]));
            }
        }
    }
}
