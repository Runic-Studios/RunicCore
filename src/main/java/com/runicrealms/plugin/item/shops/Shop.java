package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicProfessions;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.runicitems.Stat;
import com.runicrealms.runicitems.item.*;
import com.runicrealms.runicitems.item.stats.RunicItemStatRange;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * All shops which require custom mechanics (artifact forge, item scrapper, etc.) extend this class
 */
public abstract class Shop implements Listener {

    private String title;
    private ItemGUI itemGUI;

    public Shop() {
        title = "";
        itemGUI = new ItemGUI();
    }

    public Shop(String title) {
        this.title = title;
        itemGUI = new ItemGUI();
    }

    protected void setupShop(Player pl) {
        title = "&eShop";
        itemGUI = new ItemGUI(title, 9, event -> {
        },
                RunicProfessions.getInstance()).setOption(8, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0, false);
    }

    protected void setupShop(String title, int size) {
        this.title = title;
        itemGUI = new ItemGUI(this.title, size, event -> {
        },
                RunicProfessions.getInstance()).setOption(size-1, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0, false);
    }

    protected void setupShop(String title, boolean fillSlots) {
        this.title = title;
        itemGUI = new ItemGUI(this.title, 9, event -> {
        },
                RunicProfessions.getInstance()).setOption(8, new ItemStack(Material.BARRIER),
                "&cClose", "&7Close the menu", 0, false);
        if (!fillSlots) return;
        for (int i = 0; i < 8; i++) {
            itemGUI.setOption(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE), "&7", "", 0, false);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String s) {
        this.title = s;
    }

    public ItemGUI getItemGUI() {
        return itemGUI;
    }

    public void setItemGUI(ItemGUI itemGUI) {
        this.itemGUI = itemGUI;
    }

    public String generateItemLore(RunicItem item) {
        if (item instanceof RunicItemGeneric) {
            return this.generateGenericItemLore((RunicItemGeneric) item);
        }

        if (item instanceof RunicItemArmor) {
            return this.generateArmorItemLore(((RunicItemArmor) item));
        }

        if (item instanceof RunicItemWeapon) {
            return this.generateWeaponItemLore(((RunicItemWeapon) item));
        }

        if (item instanceof RunicItemOffhand) {
            return this.generateOffhandItemLore(((RunicItemOffhand) item));
        }

        return null;
    }

    private String generateGenericItemLore(RunicItemGeneric item) {
        String lore = "";

        for (String s : item.getLore()) {
            lore = lore.concat(s) + "\n";
        }

        return lore;
    }

    private String generateArmorItemLore(RunicItemArmor item) {
        String stats = "";

        stats = stats.concat(ChatColor.RED + "" + item.getHealth() + "❤\n");
        for (Stat stat : item.getStats().keySet()) {
            stats = stats.concat(stat.getChatColor() + "+" + item.getStats().get(stat).getValue() + stat.getIcon() + "\n");
        }

        return stats;
    }

    private String generateWeaponItemLore(RunicItemWeapon item) {
        String stats = "";

        RunicItemStatRange range = item.getWeaponDamage();
        stats = stats.concat(ChatColor.RED + "" + range.getMin() + "-" + range.getMax() + " DMG\n");
        for (Stat stat : item.getStats().keySet()) {
            stats = stats.concat(stat.getChatColor() + "+" + item.getStats().get(stat).getValue() + stat.getIcon() + "\n");
        }

        return stats;
    }

    private String generateOffhandItemLore(RunicItemOffhand item) {
        String stats = "";

        for (Stat stat : item.getStats().keySet()) {
            stats = stats.concat(stat.getChatColor() + "+" + item.getStats().get(stat).getValue() + stat.getIcon() + "\n");
        }

        return stats;
    }
}
