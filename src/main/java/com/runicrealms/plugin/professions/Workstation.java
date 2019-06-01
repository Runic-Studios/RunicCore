package com.runicrealms.plugin.professions;

import com.runicrealms.plugin.item.ItemGUI;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public abstract class Workstation extends ItemGUI {

    public Workstation() {
        super();
    }

    public Workstation(String name, int size, OptionClickEventHandler handler, Plugin plugin) {
        super(name, size, handler, plugin);
    }

    public void createCraftableItem() {
        super.setOption(5, new ItemStack(Material.DIAMOND), "&aDiamond", "&7Test", 0);
    }
}
