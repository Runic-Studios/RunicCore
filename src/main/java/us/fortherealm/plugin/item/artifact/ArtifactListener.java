package us.fortherealm.plugin.item.artifact;

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
import us.fortherealm.plugin.attributes.AttributeUtil;

public class ArtifactListener implements Listener {

    // opens the artifact editor
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {

        Player player = (Player) e.getWhoClicked();
        int itemslot = e.getSlot();

        // only listen for the artifact slot
        if (itemslot != 0) return;

        // don't trigger if there's no item in the slot to avoid null issues
        if (player.getInventory().getItem(0) == null) return;

        // only activate in survival mode to save builders the headache
        if (player.getGameMode() != GameMode.SURVIVAL) return;

        // only listen for a player inventory
        if (e.getClickedInventory() == null) return;
        if (!(e.getClickedInventory().getType().equals(InventoryType.PLAYER))) return;

        // cancel the event, open the artifact
        e.setCancelled(true);
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1);
        ArtifactGUI.CUSTOMIZE_ARTIFACT.open(player);
    }

    // cancel artifact swapping
    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent e) {

        Player p = e.getPlayer();
        int slot = p.getInventory().getHeldItemSlot();

        if (slot == 0) {
            e.setCancelled(true);
            p.playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            p.sendMessage(ChatColor.GRAY + "You cannot perform this action in this slot.");
        }
    }

    // cancel artifact dropping, rune dropping, hearthstone dropping
    @EventHandler
    public void onSoulboundItemDrop(PlayerDropItemEvent e) {

        Player pl = e.getPlayer();
        boolean isSoulbound = false;
        String souldbound = AttributeUtil.getCustomString(e.getItemDrop().getItemStack(), "soulbound");
        if (souldbound.equals("true")) {
            isSoulbound = true;
        }

        if (isSoulbound && pl.getGameMode() == GameMode.SURVIVAL) {
            e.setCancelled(true);
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            pl.sendMessage(ChatColor.GRAY + "This item is soulbound.");
        }
    }
}
