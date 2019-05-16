package com.runicrealms.plugin.professions;

import com.runicrealms.plugin.professions.alchemist.CauldronGUI;
import com.runicrealms.plugin.professions.blacksmith.AnvilGUI;
import com.runicrealms.plugin.professions.blacksmith.FurnaceGUI;
import com.runicrealms.plugin.professions.leatherworker.TanningRackGUI;
import com.runicrealms.plugin.professions.tailor.SpinningWheelGUI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.professions.jeweler.BenchGUI;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class WorkstationListener implements Listener {

    private static HashMap<UUID, Location> stationLocation = new HashMap<>();

    /**
     * This class communicates with the 'workstations.yml' data file
     * to manage all five profession workstations and bring up their respective GUIS/
     * play their respective sounds
     * @author Skyfallin_
     */
    @EventHandler
    public void onOpenInventory(PlayerInteractEvent e) {

        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!(e.getHand() == EquipmentSlot.HAND)) return;
        if (!e.hasBlock()) return;

        Player pl = e.getPlayer();
        Block block = e.getClickedBlock();
        Location blockLoc = block.getLocation();
        World world = block.getWorld();
        double x = blockLoc.getX();
        double y = blockLoc.getY();
        double z = blockLoc.getZ();

        // retrieve the data file
        File workstations = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "workstations.yml");
        FileConfiguration stationConfig = YamlConfiguration.loadConfiguration(workstations);
        ConfigurationSection stationLocs = stationConfig.getConfigurationSection("Workstations.Locations");

        if (stationLocs == null) return;

        // iterate through data file, if it finds a saved station w/ same world, x, y, and z, then
        // it checks 'type' and switch statement opens correct ItemGUI
        String stationType = "";
        for (String stationID : stationLocs.getKeys(false)) {

            World savedWorld = Bukkit.getServer().getWorld(stationLocs.getString(stationID + ".world"));
            double savedX = stationLocs.getDouble(stationID + ".x");
            double savedY = stationLocs.getDouble(stationID + ".y");
            double savedZ = stationLocs.getDouble(stationID + ".z");

            // if this particular location is saved, check what kind of workstation it is
            if (world == savedWorld && x == savedX && y == savedY && z == savedZ){
                String savedStation = stationLocs.getString(stationID + ".type");
                if (savedStation == null) return;
                stationType = savedStation;
            }
        }

        // if we've found a workstation, open the associated ItemGUI
        if (!stationType.equals("")) {
            e.setCancelled(true);
            tryOpenGUI(pl, block, stationType);
        }
    }

    private void tryOpenGUI(Player pl, Block block, String stationType) {

        UUID uuid = pl.getUniqueId();

        // determine the player's profession
        String className = RunicCore.getInstance().getConfig().get(pl.getUniqueId() + ".info.prof.name").toString();

        // stop the listener if the player is already crafting
        if (RunicCore.getProfManager().getCurrentCrafters().contains(pl)) {
            pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            pl.sendMessage(ChatColor.RED + "You are currently crafting.");
            return;
        }

        // check which stationType is being called
        switch (stationType) {
            case "anvil":
                if (className.equals("Blacksmith")) {
                    pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_PLACE, 0.5f, 1.0f);
                    AnvilGUI.ANVIL_GUI.open(pl);
                } else {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.RED + "A blacksmith would know how to use this.");
                }
                break;
            case "cauldron":
                pl.playSound(pl.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 0.5f, 0.25f);
                CauldronGUI.CAULDRON_GUI.open(pl);
                break;
            case "furnace":
                if (className.equals("Blacksmith")) {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 0.5f, 0.5f);
                    pl.playSound(pl.getLocation(), Sound.ITEM_BUCKET_FILL_LAVA, 0.5f, 1.0f);
                    pl.playSound(pl.getLocation(), Sound.BLOCK_LAVA_POP, 0.5f, 1.0f);
                    FurnaceGUI.FURNACE_GUI.open(pl);
                } else {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.RED + "A blacksmith would know how to use this.");
                }
                break;
            case "gemcutting bench":
                if (className.equals("Jeweler")) {
                    pl.playSound(pl.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 2.0f);
                    BenchGUI.BENCH_GUI.open(pl);
                } else {
                    pl.playSound(pl.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                    pl.sendMessage(ChatColor.RED + "A jeweler would know how to use this.");
                }
                break;
            case "spinning wheel":
                pl.playSound(pl.getLocation(), Sound.BLOCK_WET_GRASS_BREAK, 2.0f, 1.2f);
                SpinningWheelGUI.SPINNING_WHEEL_GUI.open(pl);
                break;
            case "tanning rack":
                pl.playSound(pl.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 2.0f, 0.8f);
                TanningRackGUI.TANNING_RACK_GUI.open(pl);
                break;
        }

        // add station location to the hashmap for item display purposes
        Location loc = block.getLocation().add(0.5, 1.0, 0.5);
        stationLocation.remove(uuid);
        stationLocation.put(uuid, loc);
    }

    /**
     * This event adds a new workstation to the file, so long as the player is opped and holding a green wool.
     * The event then listens for the player's chat response, and adds the block to the file accordingly.
     */
    private ArrayList<UUID> chatters = new ArrayList<>();
    @EventHandler
    public void onLocationAdd(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        if (!pl.isOp()) return;

        if (pl.getInventory().getItemInMainHand() == null) return;
        Material heldItemType = pl.getInventory().getItemInMainHand().getType();
        if (heldItemType != Material.GREEN_WOOL) return;
        if (!e.hasBlock()) return;
        Block b = e.getClickedBlock();

        // retrieve the data file
        File workstations = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "workstations.yml");
        FileConfiguration stationConfig = YamlConfiguration.loadConfiguration(workstations);

        // save data file
        try {
            stationConfig.save(workstations);
        } catch (IOException ex) {
            ex.printStackTrace();
        }


        // todo: change to first available ID
        if (!stationConfig.isSet("Workstations.NEXT_ID")) {
            stationConfig.set("Workstations.NEXT_ID", 0);
        }
        int nextID = stationConfig.getInt("Workstations.NEXT_ID");
        stationConfig.set("Workstations.Locations." + nextID + ".world" , b.getWorld().getName());
        stationConfig.set("Workstations.Locations." + nextID + ".x", b.getLocation().getBlockX());
        stationConfig.set("Workstations.Locations." + nextID + ".y", b.getLocation().getBlockY());
        stationConfig.set("Workstations.Locations." + nextID + ".z", b.getLocation().getBlockZ());
        pl.sendMessage(ChatColor.GREEN + "Workstation saved! Now please specify the type of this workstation:\n"
                + "Anvil, cauldron, furnace, gemcutting bench, spinning wheel, or tanning rack?");
        chatters.add(pl.getUniqueId());

        // save data file
        try {
            stationConfig.save(workstations);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player pl = e.getPlayer();
        if (this.chatters.contains(pl.getUniqueId())) {

            e.setCancelled(true);

            // retrieve chat message
            String stationType = e.getMessage().toLowerCase();

            // verify input
            if (!(stationType.equals("anvil")
                    || stationType.equals("cauldron")
                    || stationType.equals("furnace")
                    || stationType.equals("gemcutting bench")
                    || stationType.equals("spinning wheel")
                    || stationType.equals("tanning rack"))) {
                pl.sendMessage(ChatColor.RED + "Please specify a correct input.");
                return;
            }

            // retrieve the data file
            File workstations = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                    "workstations.yml");
            FileConfiguration stationConfig = YamlConfiguration.loadConfiguration(workstations);

            if (!stationConfig.isSet("Workstations.NEXT_ID")) {
                stationConfig.set("Workstations.NEXT_ID", 0);
            }
            int nextID = stationConfig.getInt("Workstations.NEXT_ID");

            stationConfig.set("Workstations.Locations." + nextID + ".type", stationType);
            stationConfig.set("Workstations.NEXT_ID", nextID+1);
            pl.sendMessage(ChatColor.GREEN + "Workstation type set to: " + ChatColor.YELLOW + stationType);
            chatters.remove(pl.getUniqueId());

            // save data file
            try {
                stationConfig.save(workstations);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * This event disables the vanilla functionality of: anvils, brewing stands, cauldrons,
     * crafting tables,  furnaces.
     */
    @EventHandler
    public void onDefaultStationUse(PlayerInteractEvent e) {

        if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        if (!e.hasBlock()) return;
        Block b = e.getClickedBlock();
        if (b.getType() == Material.ANVIL
                || b.getType() == Material.BREWING_STAND
                || b.getType() == Material.CAULDRON
                || b.getType() == Material.CRAFTING_TABLE
                || b.getType() == Material.FURNACE) {
            e.setCancelled(true);
        }
    }

    public static HashMap<UUID, Location> getStationLocation() {
        return stationLocation;
    }
}
