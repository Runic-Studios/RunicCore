//package us.fortherealm.plugin.professions;
//
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.configuration.file.FileConfiguration;
//import org.bukkit.configuration.file.YamlConfiguration;
//import org.bukkit.entity.Player;
//import org.bukkit.event.EventHandler;
//import org.bukkit.event.Listener;
//import org.bukkit.event.block.BlockBreakEvent;
//import org.bukkit.plugin.Plugin;
//import us.fortherealm.plugin.Main;
//
//import java.io.File;
//import java.io.IOException;
//
//public class ResourceAdder implements Listener {
//
//    Plugin plugin = Main.getInstance();
//
//    @EventHandler
//    public void addOre(BlockBreakEvent e) {
//
//        Block block = e.getBlock();
//        Location bLoc = block.getLocation();
//        Player pl = e.getPlayer();
//
//        if (!pl.isOp()) return;
//
//        if (pl.getInventory().getItemInMainHand().getType() == Material.FLINT) {
//
//            e.setCancelled(true);
//            pl.sendMessage("You added an ore at " + bLoc);
//
//            // todo: finish this.
//
//        }
//
//    }
//}
