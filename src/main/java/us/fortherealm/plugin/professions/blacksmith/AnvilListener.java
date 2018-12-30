package us.fortherealm.plugin.professions.blacksmith;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import us.fortherealm.plugin.Main;

import java.util.HashMap;
import java.util.UUID;

public class AnvilListener implements Listener {

    private static HashMap<UUID, Location> anvilLocation = new HashMap<>();

    /**
     * class which controls use of anvils and brings up blacksmith station
     */
    @EventHandler
    public void onOpenInventory(PlayerInteractEvent e) {

        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;

        if (!e.hasBlock()) return;

        if (!(e.getClickedBlock().getType().equals(Material.ANVIL))) return;

        Player pl = e.getPlayer();
        UUID uuid = pl.getUniqueId();

        // determine the player's profession
        String className = Main.getInstance().getConfig().get(pl.getUniqueId() + ".info.prof.name").toString();

        // cancel the event
        e.setCancelled(true);
        pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.0f);

        if (className.equals("Blacksmith")) {
            Location loc = e.getClickedBlock().getLocation();
            Location center = loc.add(0.5, 1.0, 0.5);
            anvilLocation.put(uuid, center);
            AnvilGUI.ANVIL_GUI.open(pl);
        } else {
            pl.sendMessage(ChatColor.RED + "A blacksmith would know how to use this.");
        }
    }

    public static HashMap<UUID, Location> getAnvilLocation() {
        return anvilLocation;
    }
}
