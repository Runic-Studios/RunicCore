package us.fortherealm.plugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

/**
 * This listener disables all vanilla crafting for player who aren't in creative move,
 * since we don't use the default crafting systems anymore.
 */
public class CraftingListener implements Listener {

    @EventHandler
    public void onCraft(CraftItemEvent e) {

        // only listen for players
        if (!(e.getWhoClicked() instanceof Player)) return;

        Player pl = (Player) e.getWhoClicked();

        if (pl.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }
}
