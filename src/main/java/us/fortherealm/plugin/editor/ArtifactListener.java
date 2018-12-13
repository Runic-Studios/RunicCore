package us.fortherealm.plugin.editor;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

public class ArtifactListener implements Listener {

    // opens the editor editor
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // only listen for the editor slot
        if (itemslot != 0) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (player.getInventory().getItem(0) == null) return;

        // only activate in survival mode to save builders the headache
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        // cancel the event, open the editor
        e.setCancelled(true);
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1);
        ArtifactGUI.CUSTOMIZE_ARTIFACT.open(player);
    }

    // cancel editor swapping
    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent swapevent) {

        Player p = swapevent.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        if (slot == 0) {
            swapevent.setCancelled(true);
            p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            p.sendMessage(ChatColor.RED + "You cannot perform this action in this slot.");
        }
    }

    // cancel editor dropping
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {

        Player player = e.getPlayer();
        int slot = player.getInventory().getHeldItemSlot();

        if (slot == 0 && player.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You cannot drop your editor.");
        }
    }
}
