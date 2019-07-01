package com.runicrealms.plugin.item;

import java.util.ArrayList;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ChatUtils;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

public class ItemGUI implements Listener {

    private String name;
    private int size;
    private OptionClickEventHandler handler;
    private Plugin plugin;

    private ClickType clickType;
    private String[] optionNames;
    private ItemStack[] optionIcons;
    private Inventory inventory;

    public ItemGUI() {
        this.name = ColorUtil.format("&7Default Menu");
        this.size = 9;
        this.handler = (OptionClickEvent event) -> {};
        this.plugin = RunicCore.getInstance();
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public ItemGUI(String name, int size, OptionClickEventHandler handler, Plugin plugin) {
        this.name = ColorUtil.format(name);
        this.size = size;
        this.handler = handler;
        this.plugin = plugin;
        this.optionNames = new String[size];
        this.optionIcons = new ItemStack[size];
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /*
    Getters and setters! Hooray.
     */
    public ClickType getClickType() { return clickType; }

    public ItemStack getItem(int index) {
        return inventory.getItem(index);
    }

    public String getName() { return this.name; }

    public void setName(String name) { this.name = ColorUtil.format(name); }




    public void setSize(int size) {
        this.size = size;
    }

    public void setHandler(OptionClickEventHandler handler) {
        this.handler = handler;
    }

    public ItemGUI setOption(int position, ItemStack icon, String name, String desc, int durability) {
        optionNames[position] = name;
        optionIcons[position] = setItemNameAndLore(icon, name, desc, durability);
        return this;
    }

    public void open(Player player) {
        inventory = Bukkit.createInventory(player, size, name);
        for (int i = 0; i < optionIcons.length; i++) {
            if (optionIcons[i] != null) {
                inventory.setItem(i, optionIcons[i]);
            }
        }
        player.openInventory(inventory);
    }

    public void destroy() {
        HandlerList.unregisterAll(this);
        handler = null;
        plugin = null;
        optionNames = null;
        optionIcons = null;
    }

    @EventHandler(priority=EventPriority.MONITOR)
    void onInventoryClick(InventoryClickEvent event) {

        this.clickType = event.getClick();

        if (event.getInventory().getTitle().equals(this.name)) {

            // ----------------------------------------------------------------------
            // FIXES A SUBSTANTIAL BUG: STEALING ARTIFACT SKINS FOR MAX ATTACK SPEED
            if(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
                event.getWhoClicked().closeInventory();
                event.setCancelled(true);
                event.setResult(Event.Result.DENY);
                ((Player) event.getWhoClicked()).updateInventory();
            }
            // ----------------------------------------------------------------------

            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
            int slot = event.getRawSlot();
            if (slot >= 0 && slot < size && optionNames[slot] != null) {
                Plugin plugin = this.plugin;

                OptionClickEvent e = new OptionClickEvent(event, (Player)event.getWhoClicked(), slot, optionNames[slot]);
                handler.onOptionClick(e);

                if (e.willClose()) {
                    final Player p = (Player)event.getWhoClicked();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, p::closeInventory, 1);
                }
                if (e.willDestroy()) {
                    destroy();
                }
            }
        }
    }


    public interface OptionClickEventHandler {
        public void onOptionClick(OptionClickEvent event);
    }

    /**
     * @param item - the item in the menu
     * @param name - name to display, accepts color codes
     * @param desc - split lines with "\n", accepts color codes
     * @param durability - of the item
     */
    private ItemStack setItemNameAndLore(ItemStack item, String name, String desc, int durability) {

        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        for (String line : desc.split("\n")) {

            line = ColorUtil.format(line);
            lore.add(line);
        }

        if (meta != null) {
            meta.setLore(lore);
            meta.setDisplayName(ColorUtil.format(name));
            ((Damageable) meta).setDamage(durability);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            item.setItemMeta(meta);
        }

        return item;
    }
}