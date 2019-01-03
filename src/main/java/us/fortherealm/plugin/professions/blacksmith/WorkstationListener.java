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

public class WorkstationListener implements Listener {

    private static HashMap<UUID, Location> stationLocation = new HashMap<>();

    /**
     * class which controls use of anvils/furnaces and brings up blacksmith station
     */
    @EventHandler
    public void onOpenInventory(PlayerInteractEvent e) {

        if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;

        if (!e.hasBlock()) return;

        Material blockType = e.getClickedBlock().getType();

        // listens for anvils/furnaces
        if (!(blockType.equals(Material.ANVIL) || blockType.equals(Material.FURNACE))) return;

        Player pl = e.getPlayer();
        UUID uuid = pl.getUniqueId();

        // determine the player's profession
        String className = Main.getInstance().getConfig().get(pl.getUniqueId() + ".info.prof.name").toString();

        // cancel the event
        e.setCancelled(true);

        // stop the listener if the player is already crafting
        if (Main.getProfManager().getCurrentCrafters().contains(pl)) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.RED + "You are currently crafting.");
            return;
        }

        if (className.equals("Blacksmith")) {
            if (blockType.equals(Material.ANVIL)) {
                pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.0f);
                AnvilGUI.ANVIL_GUI.open(pl);
            } else {
                pl.playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                pl.playSound(pl.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 0.5f, 1.0f);
                pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 1.0f);
                FurnaceGUI.FURNACE_GUI.open(pl);
            }
            Location loc = e.getClickedBlock().getLocation().add(0.5, 1.0, 0.5);
            stationLocation.remove(uuid);
            stationLocation.put(uuid, loc);
        } else {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.RED + "A blacksmith would know how to use this.");
        }
    }

    public static HashMap<UUID, Location> getStationLocation() {
        return stationLocation;
    }
}
