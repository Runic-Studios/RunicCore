package com.runicrealms.plugin.item;

import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

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
        if (AttributeUtil.getCustomString(offhand, "offhand").equals("true")) {
            armorAndOffhand.add(pl.getInventory().getItemInOffHand());
        }
        return armorAndOffhand;
    }

    public static int getHealthBoost(Player pl) {
        int healthBoost = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            healthBoost += (int) AttributeUtil.getCustomDouble(item, "custom.maxHealth");
        }
        return healthBoost;
    }

    public static int getHealthRegenBoost(Player pl) {
        int healthRegenBoost = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            healthRegenBoost += (int) AttributeUtil.getCustomDouble(item, "custom.healthRegen");
        }
        return healthRegenBoost;
    }

    public static int getManaBoost(Player pl) {
        int manaBoost = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            manaBoost += (int) AttributeUtil.getCustomDouble(item, "custom.manaBoost");
        }
        return manaBoost;
    }

    public static int getManaRegenBoost(Player pl) {
        int manaRegen = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            manaRegen += (int) AttributeUtil.getCustomDouble(item, "custom.manaRegen");
        }
        return manaRegen;
    }

    public static int getAttackBoost(Player pl) {
        int attackDamage = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            attackDamage += (int) AttributeUtil.getCustomDouble(item, "custom.attackDamage");
        }
        return attackDamage;
    }

    public static int getMinDamage(Player pl) {
        ItemStack item = pl.getInventory().getItemInMainHand();
        return (int) AttributeUtil.getCustomDouble(item, "custom.minDamage");
    }

    public static int getMaxDamage(Player pl) {
        ItemStack item = pl.getInventory().getItemInMainHand();
        return (int) AttributeUtil.getCustomDouble(item, "custom.maxDamage");
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

    public static int getShieldAmt(Player pl) {
        int shieldAmt = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            shieldAmt += (int) AttributeUtil.getCustomDouble(item, "custom.shield");
        }
        return shieldAmt;
    }

    public static int getCritEnchant(Player pl) {
        int critEnchant = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            if (AttributeUtil.getCustomString(item, "scroll.enchantment").equalsIgnoreCase("crit"))
                critEnchant += (int) AttributeUtil.getCustomDouble(item, "scroll.percent");
        }
        return critEnchant;
    }

    public static int getDodgeEnchant(Player pl) {
        int dodgeEnchant = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            if (AttributeUtil.getCustomString(item, "scroll.enchantment").equalsIgnoreCase("dodge"))
                dodgeEnchant += (int) AttributeUtil.getCustomDouble(item, "scroll.percent");
        }
        return dodgeEnchant;
    }

    public static int getSpeedEnchant(Player pl) {
        int speedEnchant = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            if (AttributeUtil.getCustomString(item, "scroll.enchantment").equalsIgnoreCase("speed"))
                speedEnchant += (int) AttributeUtil.getCustomDouble(item, "scroll.percent");
        }
        return speedEnchant;
    }

    public static int getThornsEnchant(Player pl) {
        int thornsEnchant = 0;
        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            if (AttributeUtil.getCustomString(item, "scroll.enchantment").equalsIgnoreCase("thorns"))
                thornsEnchant += (int) AttributeUtil.getCustomDouble(item, "scroll.percent");
        }
        return thornsEnchant;
    }
}
