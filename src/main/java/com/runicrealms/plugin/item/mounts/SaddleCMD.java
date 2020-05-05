package com.runicrealms.plugin.item.mounts;

import com.runicrealms.plugin.attributes.AttributeUtil;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashMap;

public class SaddleCMD {

    public static void giveSaddle(Player pl, HorseTypeEnum horseTypeEnum) {
        ItemStack mountSaddle = mountSaddle(horseTypeEnum);
        HashMap<Integer, ItemStack> itemsToAdd = pl.getInventory().addItem(mountSaddle);
        // drop leftover items on the floor
        for (ItemStack leftOver : itemsToAdd.values()) {
            pl.getWorld().dropItem(pl.getLocation(), leftOver);
        }
    }

    private static ItemStack mountSaddle(HorseTypeEnum horseType) {

        ItemStack mountSaddle = new ItemStack(Material.SADDLE);

        // tell the game this is a custom saddle, add the horse type
        mountSaddle = AttributeUtil.addCustomStat(mountSaddle, "mount", "true");
        mountSaddle = AttributeUtil.addCustomStat(mountSaddle, "horseType", horseType.toString());

        ItemMeta meta = mountSaddle.getItemMeta();
        if (meta == null) return null;

        String tier = horseType.getTier().toString().substring(0, 1).toUpperCase().
                concat(horseType.getTier().toString().substring(1).toLowerCase());
        ChatColor color;
        if (horseType.getTier().equals(HorseTierEnum.NORMAL)) {
            color = ChatColor.GRAY;
        } else if (horseType.getTier().equals(HorseTierEnum.EPIC)) {
            color = ChatColor.LIGHT_PURPLE;
        } else {
            color = ChatColor.GOLD;
        }

        meta.setDisplayName(ChatColor.YELLOW + horseType.getName());
        meta.setLore(Arrays.asList(
                org.bukkit.ChatColor.GRAY + "",
                org.bukkit.ChatColor.GOLD + "Tier: " + color + tier,
                org.bukkit.ChatColor.GOLD + "Speed: +" + color + percent(horseType.getTier().getSpeed()) +"%",
                org.bukkit.ChatColor.GRAY + "",
                org.bukkit.ChatColor.GRAY + "" + ChatColor.ITALIC + "A loyal and valiant steed!",
                org.bukkit.ChatColor.GRAY + "",
                org.bukkit.ChatColor.YELLOW + "Unique"
        ));
        mountSaddle.setItemMeta(meta);

        return mountSaddle;
    }

    public static int percent(float speed) {
        return ((int) Math.round(((speed) - 0.20) * 500));
    }
}
