package com.runicrealms.plugin.donator;

import com.runicrealms.plugin.common.util.ColorUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class BoostsUI {

    private static ItemStack topElement;

    static {
        topElement = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = topElement.getItemMeta();
        meta.setDisplayName("&cNetwork Boosts");
        meta.setLore(List.of(
                ColorUtil.format("&7All online players are boosted for a limited duration"),
                ColorUtil.format("&7View combat, crafting, and gathering boosts"),
                ColorUtil.format("&7Purchase more boosts at &estore.runicrealms.com")));
        topElement.setItemMeta(meta);
        
    }
}
