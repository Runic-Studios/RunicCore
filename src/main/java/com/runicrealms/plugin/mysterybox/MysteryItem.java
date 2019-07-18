package com.runicrealms.plugin.mysterybox;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by KissOfFate
 * Date: 7/17/2019
 * Time: 8:06 PM
 */
public class MysteryItem {
    private ItemStack _item;
    private double _weight;
    private String _command;

    public MysteryItem(ItemStack item, String command, double weight) {
        this._item = item;
        this._command = command;
        this._weight = weight;
    }

    public ItemStack getItemStack() {
        return this._item;
    }

    public String getCommand() {
        return this._command;
    }

    public void executeCommand(Player player) {
        Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), getCommand().replace("{player}", player.getName()));
    }

    public double getWeight() {
        return this._weight;
    }
}
