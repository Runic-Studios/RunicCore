package com.runicrealms.plugin.mounts;

import com.runicrealms.plugin.attributes.AttributeUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SaddleCMD {

    public static void giveSaddle(String color, HorseTypeEnum type) {

        ItemStack mountSaddle = new ItemStack(Material.SADDLE);
        ItemMeta meta = mountSaddle.getItemMeta();
        if (meta == null) return;

        // tell the game this is a custom saddle
        mountSaddle = AttributeUtil.addCustomStat(mountSaddle, "mount", "true");

    }
}
