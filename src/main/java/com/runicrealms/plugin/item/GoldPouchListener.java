package com.runicrealms.plugin.item;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.attributes.AttributeUtil;
import com.runicrealms.plugin.events.PouchCloseEvent;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.utilities.CurrencyUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Manages the gold pouch item
 */
public class GoldPouchListener implements Listener {

    @EventHandler
    public void onPouchInteract(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        if (pl.getGameMode() != GameMode.SURVIVAL) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!e.hasItem()) return;
        if (!e.getItem().hasItemMeta()) return;

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
                    "&7", "", 0);
        }

        coinMenu.setHandler(event -> {

            if (event.getCurrentItem().getType() == Material.BLACK_STAINED_GLASS_PANE) {
                event.setWillClose(false);
                e.setCancelled(true);
            } else {
                e.setCancelled(false);
            }
        });

        coinMenu.open(pl);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getInventory().getTitle().toLowerCase().contains("gold pouch")) {

            ItemStack pouch = e.getPlayer().getInventory().getItemInMainHand();

            int amount = 0;
            for (ItemStack is : e.getInventory()) {
                if (is == null) continue;
                if (is.getType() != Material.GOLD_NUGGET) continue;
                amount += is.getAmount();
            }

            PouchCloseEvent event = new PouchCloseEvent((Player) e.getPlayer(), pouch, amount);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                return;
            }

            // update new current amount
            pouch = AttributeUtil.addCustomStat(pouch, "goldAmount", event.getAmount());
            LoreGenerator.generateGoldPouchLore(pouch);
            e.getPlayer().getInventory().setItem(e.getPlayer().getInventory().getHeldItemSlot(), pouch);
        }
    }
}
