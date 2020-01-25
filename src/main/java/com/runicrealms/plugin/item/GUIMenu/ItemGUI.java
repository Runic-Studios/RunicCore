package com.runicrealms.plugin.item.GUIMenu;

import java.util.ArrayList;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String[] getOptionNames() {
        return optionNames;
    }

    public void setOptionNames(String[] optionNames) {
        this.optionNames = optionNames;
    }

    public OptionClickEventHandler getHandler() {
        return this.handler;
    }

    public void setHandler(OptionClickEventHandler handler) {
        this.handler = handler;
    }

    public ItemGUI setOption(int position, ItemStack icon, String name, String desc, int durability, boolean isGlowing) {
        optionNames[position] = name;
        optionIcons[position] = setItemNameAndLore(icon, name, desc, durability, isGlowing);
        return this;
    }

    public ItemGUI setOption(int position, ItemStack icon) {
        optionIcons[position] = icon;
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

//    @EventHandler(priority=EventPriority.MONITOR)
//    void onInventoryClick(InventoryClickEvent event) {
//
//        this.clickType = event.getClick();
//
//        if (event.getInventory().getTitle().equals(this.name)) {
//
//            // gold scrapper
//            if (event.getInventory().getTitle().toLowerCase().contains("scrapper")) {
//
//                // menu items in gold scrapper menu
//                if (event.getCurrentItem() != null
//                        && (event.getCurrentItem().getType() == Material.SLIME_BALL
//                        || event.getCurrentItem().getType() == Material.BARRIER)) {
//                        event.setCancelled(true);
//
//                } else if (event.getCurrentItem() != null) {
//
//                    if (event.getCurrentItem().getType() == Material.AIR) {
//                        event.setCancelled(false);
//
//                    } else {
//
//                        ItemTypeEnum itemType = ItemTypeEnum.matchType(event.getCurrentItem());
//                        switch (itemType) {
//                            case PLATE:
//                            case GILDED:
//                            case MAIL:
//                            case LEATHER:
//                            case CLOTH:
//                                if (soulboundCheck(event)) {
//                                    event.setCancelled(false);
//                                } else {
//                                    event.setCancelled(true);
//                                    event.getWhoClicked().sendMessage
//                                            (ChatColor.GRAY + "[1/1] "  +
//                                                    ChatColor.YELLOW + "Armor Scrapper: " +
//                                                    ChatColor.WHITE + "I can only scrap armor!");
//                                    ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(),
//                                            Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
//                                }
//                                break;
//                            default:
//                                event.setCancelled(true);
//                                event.getWhoClicked().sendMessage
//                                        (ChatColor.GRAY + "[1/1] "  +
//                                                ChatColor.YELLOW + "Armor Scrapper: " +
//                                                ChatColor.WHITE + "I can only scrap armor!");
//                                ((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(),
//                                        Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
//                                break;
//                        }
//                    }
//                }
//
//                // loot chests
//            } else if (event.getInventory().getTitle().toLowerCase().contains("chest")) {
//
//                // cancel moving soulbound items to chest
//                if((event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) && !soulboundCheck(event)) {
//                    event.setCancelled(true);
//                } else if (!soulboundCheck(event)) {
//                    event.setCancelled(true);
//                } else {
//                    event.setCancelled(false);
//                }
//
//                // gold pouch
//            } else if (event.getInventory().getTitle().toLowerCase().contains("gold pouch")
//                    && event.getCurrentItem() != null
//                    && (event.getCurrentItem().getType() == Material.GOLD_NUGGET
//                    || (event.getCursor().getType() == Material.GOLD_NUGGET
//                    && event.getCurrentItem().getType() == Material.AIR
//                    || event.getCurrentItem().getType() == Material.GOLD_NUGGET))) {
//                event.setCancelled(false);
//
//                // all other menus
//            } else {
//                event.setCancelled(true);
//                event.setResult(Event.Result.DENY);
//
//                // ----------------------------------------------------------------------
//                // FIXES A SUBSTANTIAL BUG: STEALING ARTIFACT SKINS FOR MAX ATTACK SPEED
//                if(event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)) {
//                    event.getWhoClicked().closeInventory();
//                    event.setCancelled(true);
//                    event.setResult(Event.Result.DENY);
//                    ((Player) event.getWhoClicked()).updateInventory();
//                }
//                // ----------------------------------------------------------------------
//
//            }
//
//            int slot = event.getRawSlot();
//            if (slot >= 0 && slot < size && optionNames[slot] != null) {
//                Plugin plugin = this.plugin;
//
//                OptionClickEvent e = new OptionClickEvent(event, (Player)event.getWhoClicked(), slot, optionNames[slot]);
//                handler.onOptionClick(e);
//
//                if (e.willClose()) {
//                    final Player p = (Player)event.getWhoClicked();
//                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, p::closeInventory, 1);
//                }
//                if (e.willDestroy()) {
//                    destroy();
//                }
//            }
//        }
//    }

    /**
     * This method is called when a player closes a GUI.
     * This fixed all kinds of bugs with inventory GUIS.
     * IMPORTANT: NEVER call closeInventory. Always wrap it in a delayed task by 1 tick.
     * Contact Spigot Devs to explain this, I don't got the time ;)
     * @author Skyfallin_
     */
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getTitle().equals(this.name)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    InventoryView view = e.getPlayer().getOpenInventory();
                    if (view.getTopInventory().toString().contains("CraftInventoryCrafting")) {
                        e.getPlayer().closeInventory();
                        destroy();
                    }
                }
            }.runTaskLater(plugin, 1L);
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
    private ItemStack setItemNameAndLore(ItemStack item, String name, String desc, int durability, boolean isGlowing) {

        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<>();

        if (!desc.equals("")) {
            for (String line : desc.split("\n")) {

                line = ColorUtil.format(line);
                lore.add(line);
            }
        }

        if (meta != null) {
            meta.setLore(lore);
            meta.setDisplayName(ColorUtil.format(name));
            ((Damageable) meta).setDamage(durability);
            meta.setUnbreakable(true);
            meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            if (isGlowing) {
                meta.addEnchant(Enchantment.DURABILITY, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
        }

        return item;
    }

    private boolean soulboundCheck(InventoryClickEvent event) {
        // cancel moving soulbound items to inventories
        String soulbound = AttributeUtil.getCustomString(event.getCurrentItem(), "soulbound");
        if (soulbound.equals("true")) {
            Player pl = (Player) event.getWhoClicked();
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "This item is soulbound.");
            return false;
        }
        return true;
    }
}