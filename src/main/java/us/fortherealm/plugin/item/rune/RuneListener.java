package us.fortherealm.plugin.item.rune;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class RuneListener implements Listener {

    // opens the rune editor
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // only listen for the rune slot
        if (itemslot != 1) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (player.getInventory().getItem(1) == null) return;

        // only activate in survival mode to save builders the headache
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        // cancel the event, open the artifact
        e.setCancelled(true);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        RuneGUI.CUSTOMIZE_RUNE.open(player);
    }

    // cancel rune swapping
    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        if (slot == 1) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            p.sendMessage(ChatColor.GRAY + "You cannot perform this action in this slot.");
        }
    }
}
