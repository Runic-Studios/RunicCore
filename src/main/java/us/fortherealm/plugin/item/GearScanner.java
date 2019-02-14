package us.fortherealm.plugin.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.enums.ItemTypeEnum;

import java.util.ArrayList;

public class GearScanner {

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

    public static int getMagicBoost(Player pl) {

        int magicBoost = 0;

        ArrayList<ItemStack> armorAndOffhand = armorAndOffHand(pl);

        for (ItemStack item : armorAndOffhand) {
            magicBoost += (int) AttributeUtil.getCustomDouble(item, "custom.magicDamage");
        }

        return magicBoost;
    }
}
