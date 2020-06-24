package com.runicrealms.plugin.item.goldpouch;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.events.PouchCloseEvent;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.GUIMenu.OptionClickEvent;
import com.runicrealms.plugin.item.LoreGenerator;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

/**
 * Manages the gold pouch item
 */
public class GoldPouchListener implements Listener {

    private final HashMap<UUID, PouchMenu> pouchLookers;

    public GoldPouchListener() {
        pouchLookers = new HashMap<>();
    }

    /*
    Fixes a dupe bug
     */
    @EventHandler
    public void onGoldPouchDrop(PlayerDropItemEvent e) {
        if (pouchLookers.containsKey(e.getPlayer().getUniqueId())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage(ChatColor.RED + "You can't drop items while the gold pouch is open!");
        }
    }

    @EventHandler
    public void onPouchInteract(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;
        if (e.getItem().getItemMeta() == null) return;

        ItemStack pouch = e.getItem();
        ItemMeta meta = pouch.getItemMeta();
        int durability = ((Damageable) meta).getDamage();
        if (pouch.getType() != Material.SHEARS || durability != 234) return;

        int currentAmt = (int) AttributeUtil.getCustomDouble(pouch, "goldAmount");
        int size = (int) AttributeUtil.getCustomDouble(pouch, "pouchSize");

        int menuSize = 9;
        if (size > 576) {
            menuSize = 18;
        }

        ItemGUI coinMenu = new ItemGUI("&f" + pl.getName() + "s &6Gold Pouch", menuSize, event -> {}, RunicCore.getInstance());
        pouchLookers.put(pl.getUniqueId(), new PouchMenu(coinMenu, pouch));

        // fill coins
        int slotSize = size / 64;
        int dummyVar = currentAmt;
        for (int i = 0; i < slotSize; i++) {
            int stackSize = 64;
            if (dummyVar < 64) {
                stackSize = dummyVar;
            }
            coinMenu.setOption(i, CurrencyUtil.goldCoin(stackSize));
            dummyVar -= stackSize;
        }

        // fill black glass panes
        for (int i = slotSize; i < menuSize; i++) {
            coinMenu.setOption(i, new ItemStack(Material.BLACK_STAINED_GLASS_PANE),
                    "&7", "", 0, false);
        }

        coinMenu.setHandler(event -> {

            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
                event.setWillClose(false);
                e.setCancelled(true);
            } else {
                e.setCancelled(false);
            }
        });

        pl.playSound(pl.getLocation(), Sound.ENTITY_HORSE_SADDLE, 0.5f, 1.0f);
        coinMenu.open(pl);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!pouchLookers.containsKey(e.getWhoClicked().getUniqueId())) return;
        Player pl = (Player) e.getWhoClicked();

        e.setCancelled(!e.getView().getTitle().toLowerCase().contains("gold pouch")
                || e.getCurrentItem() == null
                || (e.getCurrentItem().getType() != Material.GOLD_NUGGET
                && ((e.getCursor().getType() != Material.GOLD_NUGGET
                || e.getCurrentItem().getType() != Material.AIR)
                && e.getCurrentItem().getType() != Material.GOLD_NUGGET)));

        ItemGUI itemGUI = pouchLookers.get(e.getWhoClicked().getUniqueId()).getMenu();
        int slot = e.getRawSlot();
        if (slot >= 0 && slot < itemGUI.getSize() && itemGUI.getOptionNames()[slot] != null) {

            OptionClickEvent ope = new OptionClickEvent(e, (Player) e.getWhoClicked(), slot, itemGUI.getOptionNames()[slot]);
            itemGUI.getHandler().onOptionClick(ope);

            if (ope.willClose()) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(), pl::closeInventory, 1);
            }
            if (ope.willDestroy()) {
                itemGUI.destroy();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer() instanceof Player
                && e.getView().getTitle().toLowerCase().contains("gold pouch")) {

            ItemStack pouch = pouchLookers.get(e.getPlayer().getUniqueId()).getPouch();

            int amount = 0;
            for (ItemStack is : e.getInventory()) {
                if (is == null)
                    continue;
                if (is.getType() != Material.GOLD_NUGGET)
                    continue;
                amount += is.getAmount();
            }

            PouchCloseEvent event = new PouchCloseEvent((Player) e.getPlayer(), pouch, amount);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled())
                return;

            // remove old item
            ItemRemover.takeItem((Player) e.getPlayer(), pouchLookers.get(e.getPlayer().getUniqueId()).getPouch(), 1);

            // update new current amount, give new item
            pouch = AttributeUtil.addCustomStat(pouch, "goldAmount", event.getAmount());
            LoreGenerator.generateGoldPouchLore(pouch);
            HashMap<Integer, ItemStack> leftOver = e.getPlayer().getInventory().addItem(pouch);
            for (ItemStack is : leftOver.values()) {
                e.getPlayer().getWorld().dropItem(e.getPlayer().getLocation(), is);
            }

            // remove from pouchlookers
            pouchLookers.remove(e.getPlayer().getUniqueId());
        }
    }
}
