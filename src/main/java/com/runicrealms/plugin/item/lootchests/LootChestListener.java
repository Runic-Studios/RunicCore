package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.GUIMenu.OptionClickEvent;
import com.runicrealms.plugin.item.rune.RuneGUI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

public class LootChestListener implements Listener {

    /**
     * This class communicates with the 'chests.yml' data file
     * to save a set of locations where chests can spawn.
     * @author Skyfallin_
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onChestInteract(PlayerInteractEvent e) {

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
        File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "chests.yml");
        FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chests);
        ConfigurationSection chestLocs = chestConfig.getConfigurationSection("Chests.Locations");

        if (chestLocs == null) return;

        // iterate through data file, if it finds a saved station w/ same world, x, y, and z, then
        // it checks 'tier' and switch statement to spawn correct loot
        String chestTier = "";
        for (String stationID : chestLocs.getKeys(false)) {

            World savedWorld = Bukkit.getServer().getWorld(chestLocs.getString(stationID + ".world"));
            double savedX = chestLocs.getDouble(stationID + ".x");
            double savedY = chestLocs.getDouble(stationID + ".y");
            double savedZ = chestLocs.getDouble(stationID + ".z");

            // if this particular location is saved, check what kind of workstation it is
            if (world == savedWorld && x == savedX && y == savedY && z == savedZ){
                String savedTier = chestLocs.getString(stationID + ".tier");
                if (savedTier == null) return;
                chestTier = savedTier;
            }
        }

        e.setCancelled(true);

        // if we've found a location and a chest, open the associated ItemGUI
        if (!chestTier.equals("") && block.getType() != Material.AIR) {
            openChestGUI(pl, chestTier);
        }
    }

    private void openChestGUI(Player pl, String tier) {

        // check which stationType is being called
        switch (tier) {
            case "common":
                ItemGUI test = new ItemGUI("&f&l" + pl.getName() + "'s &7&lCommon Chest", 27, (OptionClickEvent event) -> {

                }, RunicCore.getInstance());
                Random rand = new Random();
                int numOfItems = rand.nextInt(6 - 3) + 3; // 2 - 5
                for (int i = 0; i < numOfItems; i++) {
                    test.setOption(rand.nextInt(26), ChestLootTableUtil.commonLootTable().getRandom());
                }
                test.open(pl);
                break;
            case "uncommon":
                Bukkit.broadcastMessage("uncommon");
                break;
            case "rare":
                Bukkit.broadcastMessage("rare");
                break;
            case "epic":
                Bukkit.broadcastMessage("epic");
                break;
            default:
                Bukkit.broadcastMessage("default");
                break;
        }
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

        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        Material heldItemType = pl.getInventory().getItemInMainHand().getType();
        if (heldItemType != Material.PURPLE_WOOL) return;
        if (!e.hasBlock()) return;
        Block b = e.getClickedBlock();

        // retrieve the data file
        File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                "chests.yml");
        FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chests);

        // save data file
        try {
            chestConfig.save(chests);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (!chestConfig.isSet("Chests.NEXT_ID")) {
            chestConfig.set("Chests.NEXT_ID", 0);
        }
        int nextID = chestConfig.getInt("Chests.NEXT_ID");
        chestConfig.set("Chests.Locations." + nextID + ".world" , b.getWorld().getName());
        chestConfig.set("Chests.Locations." + nextID + ".x", b.getLocation().getBlockX());
        chestConfig.set("Chests.Locations." + nextID + ".y", b.getLocation().getBlockY());
        chestConfig.set("Chests.Locations." + nextID + ".z", b.getLocation().getBlockZ());
        pl.sendMessage(ChatColor.GREEN + "Loot chest saved! Now please specify the tier of this chest:\n"
                + "common, uncommon, rare, or epic?");
        chatters.add(pl.getUniqueId());

        // save data file
        try {
            chestConfig.save(chests);
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
            String chestTier = e.getMessage().toLowerCase();

            // verify input
            if (!(chestTier.equals("common")
                    || chestTier.equals("uncommon")
                    || chestTier.equals("rare")
                    || chestTier.equals("epic"))) {
                pl.sendMessage(ChatColor.RED + "Please specify a correct input.");
                return;
            }

            // retrieve the data file
            File chests = new File(Bukkit.getServer().getPluginManager().getPlugin("RunicCore").getDataFolder(),
                    "chests.yml");
            FileConfiguration chestConfig = YamlConfiguration.loadConfiguration(chests);

            if (!chestConfig.isSet("Chests.NEXT_ID")) {
                chestConfig.set("Chests.NEXT_ID", 0);
            }
            int nextID = chestConfig.getInt("Chests.NEXT_ID");

            chestConfig.set("Chests.Locations." + nextID + ".tier", chestTier);
            chestConfig.set("Chests.NEXT_ID", nextID+1);
            pl.sendMessage(ChatColor.GREEN + "Chest tier set to: " + ChatColor.YELLOW + chestTier);
            chatters.remove(pl.getUniqueId());

            // save data file
            try {
                chestConfig.save(chests);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
