package com.runicrealms.plugin.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.enums.ItemTypeEnum;

import java.util.ArrayList;

public class GearScanner {

    public static ArrayList<ItemStack> armor(Player pl) {

        ArrayList<ItemStack> armor = new ArrayList<>();
        PlayerInventory inv = pl.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chestplate = inv.getChestplate();
        ItemStack leggings = inv.getLeggings();
        ItemStack boots = inv.getBoots();

        // add all the items to arraylist
        if (helmet != null) armor.add(pl.getInventory().getHelmet());
        if (chestplate != null) armor.add(pl.getInventory().getChestplate());
        if (leggings != null) armor.add(pl.getInventory().getLeggings());
        if (boots != null) armor.add(pl.getInventory().getBoots());
        return armor;
    }

    public static ArrayList<ItemStack> armorAndOffHand(Player pl) {

        ArrayList<ItemStack> armorAndOffhand = new ArrayList<>();
        PlayerInventory inv = pl.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chestplate = inv.getChestplate();
        ItemStack leggings = inv.getLeggings();
        ItemStack boots = inv.getBoots();
        ItemStack offhand = inv.getItemInOffHand();

        // add all the items to arraylist
        if (helmet != null) armorAndOffhand.add(pl.getInventory().getHelmet());
        if (chestplate != null) armorAndOffhand.add(pl.getInventory().getChestplate());
        if (leggings != null) armorAndOffhand.add(pl.getInventory().getLeggings());
        if (boots != null) armorAndOffhand.add(pl.getInventory().getBoots());
        ItemTypeEnum offHandType = ItemTypeEnum.matchType(offhand);
        if (offhand != null && offHandType != ItemTypeEnum.GEMSTONE) {
            armorAndOffhand.add(pl.getInventory().getItemInOffHand());
        }
        return armorAndOffhand;
    }

    public static int getHealthBoost(Player pl) {

        int healthBoost = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            healthBoost += (int) AttributeUtil.getGenericDouble(item, "generic.maxHealth");
        }
        return healthBoost;
    }

    public static int getManaBoost(Player pl) {

        int manaBoost = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            manaBoost += (int) AttributeUtil.getCustomDouble(item, "custom.manaBoost");
        }
        return manaBoost;
    }

    public static int getAttackDamage(Player pl) {

        int attackDamage = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            attackDamage += (int) AttributeUtil.getCustomDouble(item, "custom.attackDamage");
        }
        return attackDamage;
    }

    public static int getHealingBoost(Player pl) {

        int healingBoost = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            healingBoost += (int) AttributeUtil.getCustomDouble(item, "custom.healingBoost");
        }
        return healingBoost;
    }

    public static int getMagicBoost(Player pl) {

        int magicBoost = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            magicBoost += (int) AttributeUtil.getCustomDouble(item, "custom.magicDamage");
        }
        return magicBoost;
    }
}
