package us.fortherealm.plugin.professions.jeweler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import us.fortherealm.plugin.attributes.AttributeUtil;
import us.fortherealm.plugin.enums.ArmorSlotEnum;
import us.fortherealm.plugin.item.LoreGenerator;

/**
 * This class manages the socketing of gems.
 * It checks to ensure the item has at least one open
 * gem slot.
 * @author Skyfallin_
 */
public class SocketListener implements Listener {

    // ignore durability 100 (that's harvesting tools)
    @EventHandler
    public void onItemSocket(InventoryClickEvent e) {

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        if (e.getCursor() == null || e.getCursor().getType() == Material.AIR) return;
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player pl = (Player) e.getWhoClicked();
        ItemStack heldItem = e.getCursor();
        ItemStack socketItem = e.getCurrentItem();
        Material socketItemType = socketItem.getType();
        String socketItemName = socketItem.getItemMeta().getDisplayName();

        // verify that the cursor item is a gemstone
        String isGemstone = AttributeUtil.getCustomString(heldItem, "custom.isGemstone");
        if (!isGemstone.equals("true")) return;

        // verify that the item to be socketed has open slots
        int socketCount = (int) AttributeUtil.getCustomDouble(socketItem, "custom.socketCount");
        int currentSockets = (int) AttributeUtil.getCustomDouble(socketItem, "custom.currentSockets");
        if (socketCount == 0 || currentSockets >= socketCount) {
            pl.playSound(pl.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.RED + "This item has no available sockets.");
            return;
        }

        // retrive the custom values of the two items
        double itemHealth = AttributeUtil.getGenericDouble(socketItem, "generic.maxHealth");
        double itemMana = AttributeUtil.getCustomDouble(socketItem, "custom.manaBoost");
        double gemHealth = AttributeUtil.getCustomDouble(heldItem, "custom.maxHealth");
        double gemMana = AttributeUtil.getCustomDouble(heldItem, "custom.manaBoost");

        // create a new item with updated attributes
        ItemStack newItem = new ItemStack(socketItemType);

        // fill the sockets
        newItem = AttributeUtil.addCustomStat(newItem, "custom.socketCount", socketCount);
        newItem = AttributeUtil.addCustomStat(newItem, "custom.currentSockets", currentSockets+1);
        ArmorSlotEnum itemSlot = ArmorSlotEnum.matchSlot(newItem);
        String slot;
        switch (itemSlot) {
            case HELMET:
                slot = "head";
                break;
            case CHESTPLATE:
                slot = "chest";
                break;
            case LEGGINGS:
                slot = "legs";
                break;
            case BOOTS:
                slot = "feet";
                break;
            default:
                slot = "mainhand";
                break;
        }

        // add 'da stats
        newItem = AttributeUtil.addGenericStat(newItem,
                "generic.maxHealth", itemHealth + gemHealth, slot);
        newItem = AttributeUtil.addCustomStat(newItem, "custom.manaBoost", itemMana + gemMana);
        LoreGenerator.generateItemLore(newItem, ChatColor.WHITE, socketItemName, "");

        // remove the gemstone from inventory, update the item in inventory
        e.setCancelled(true);
        e.setCurrentItem(newItem);
        e.setCursor(null);
        pl.playSound(pl.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
        pl.sendMessage(ChatColor.GREEN + "You placed your gemstone into this item's socket!");
    }
}
