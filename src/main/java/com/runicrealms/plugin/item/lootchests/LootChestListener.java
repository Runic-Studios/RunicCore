package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.runicitems.RunicItemsAPI;
import com.runicrealms.plugin.runicitems.item.RunicItem;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

/**
 * This class communicates with the 'chests.yml' data file
 * to save a set of locations where chests can spawn.
 *
 * @author Skyfallin_
 */
public class LootChestListener implements Listener {

    private static final File CHESTS = new File(RunicCore.getInstance().getDataFolder(), "chests.yml");
    private static final FileConfiguration CHESTS_CONFIG = YamlConfiguration.loadConfiguration(CHESTS);
    /**
     * This event adds a new workstation to the file, so long as the player is op and holding a green wool.
     * The event then listens for the player's chat response, and adds the block to the file accordingly.
     * NEW: If the player is holding red wool, they can remove a chest.
     */
    private final ArrayList<UUID> chatters = new ArrayList<>();

    /**
     * Admin chat handling for adding / removing loot chests
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player pl = e.getPlayer();
        if (!this.chatters.contains(pl.getUniqueId())) return;

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

        if (!CHESTS_CONFIG.isSet("Chests.NEXT_ID")) {
            CHESTS_CONFIG.set("Chests.NEXT_ID", 0);
        }
        int nextID = CHESTS_CONFIG.getInt("Chests.NEXT_ID");

        CHESTS_CONFIG.set("Chests.Locations." + nextID + ".tier", chestTier);
        CHESTS_CONFIG.set("Chests.NEXT_ID", nextID + 1);
        pl.sendMessage(ChatColor.GREEN + "Chest tier set to: " + ChatColor.YELLOW + chestTier);
        chatters.remove(pl.getUniqueId());

        // save data file
        try {
            CHESTS_CONFIG.save(CHESTS);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler(priority = EventPriority.NORMAL) // first
    public void onChestInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!(event.getHand() == EquipmentSlot.HAND)) return;
        if (!event.hasBlock()) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        Chest chest = (Chest) event.getClickedBlock().getState();
        BossChest bossChest = BossChest.getFromBlock(RunicCore.getBossTagger().getActiveBossLootChests(), chest);
        if (bossChest != null) return; // handled in BossTagger
        event.setCancelled(true);

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location blockLoc = block.getLocation();
        if (block.getType() == Material.AIR) return;

        // if (RunicCore.getLootChestManager().getQueuedChest(blockLoc) != null) return; // chest already queued
        if (RunicCore.getLootChestManager().getLootChest(blockLoc) == null)
            return; // chest doesn't match saved chest locations
        LootChest lootChest = RunicCore.getLootChestManager().getLootChest(blockLoc);

        /*
        iterate through data file, if it finds a saved station w/ same world, x, y, and z, then
        it checks 'tier' and switch statement to spawn correct loot
        if we've found a location and a chest, open the associated ItemGUI
         */
        LootChestTier lootChestTier = lootChest.getLootChestRarity();

        // verify player level
        if (player.getLevel() < lootChestTier.getMinAccessLevel()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You must be at least level " + lootChestTier.getMinAccessLevel() + " to open this.");
            return;
        }

        // verify chest has not already been looted
        Map<LootChest, Long> chestsOnCD = RunicCore.getLootChestManager().getChestsOnCDForUuid(player.getUniqueId());
        if (chestsOnCD.containsKey(lootChest)) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            long timeLeft = (lootChest.getLootChestRarity().getRespawnTimeSeconds() * 1000L) - (System.currentTimeMillis() - chestsOnCD.get(lootChest));
            player.sendMessage(ChatColor.RED + "You must wait " + (int) (timeLeft / 1000) + " seconds to loot this chest again!");
            return;
        }

        // open inv if all conditions are met, put chest on CD for player
        chestsOnCD.put(lootChest, System.currentTimeMillis()); // put chest on CD
        player.getWorld().playSound(blockLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1f, 1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        player.openInventory(new LootChestInventory(player, lootChestTier).getInventory());
    }

    /**
     * Admin functionality for adding / removing loot chests
     */
    @EventHandler
    public void onLocationAdd(PlayerInteractEvent e) {

        Player pl = e.getPlayer();
        if (!pl.isOp()) return;
        if (e.getHand() != EquipmentSlot.HAND) return;

        if (pl.getInventory().getItemInMainHand().getType() == Material.AIR) return;
        if (!e.hasBlock() || e.getClickedBlock() == null) return;
        Location chestLocation = e.getClickedBlock().getLocation();
        Material heldItemType = pl.getInventory().getItemInMainHand().getType();
        if (heldItemType == Material.RED_WOOL) {
            if (RunicCore.getLootChestManager().isLootChest(chestLocation)) {
                RunicCore.getLootChestManager().removeLootChest(e.getClickedBlock().getLocation());
                pl.sendMessage(ChatColor.GREEN + "Loot chest removed!");
                // remove
            } else {
                pl.sendMessage(ChatColor.RED + "Error: no loot chest found at location");
            }
            e.setCancelled(true);
            return;
        }
        if (heldItemType != Material.PURPLE_WOOL) return;
        e.setCancelled(true);
        Block b = e.getClickedBlock();

        // save data file
        try {
            CHESTS_CONFIG.save(CHESTS);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (!CHESTS_CONFIG.isSet("Chests.NEXT_ID")) {
            CHESTS_CONFIG.set("Chests.NEXT_ID", 0);
        }
        int nextID = CHESTS_CONFIG.getInt("Chests.NEXT_ID");
        CHESTS_CONFIG.set("Chests.Locations." + nextID + ".world", b.getWorld().getName());
        CHESTS_CONFIG.set("Chests.Locations." + nextID + ".x", b.getLocation().getBlockX());
        CHESTS_CONFIG.set("Chests.Locations." + nextID + ".y", b.getLocation().getBlockY());
        CHESTS_CONFIG.set("Chests.Locations." + nextID + ".z", b.getLocation().getBlockZ());
        pl.sendMessage(ChatColor.GREEN + "Loot chest saved! Now please specify the tier of this chest:\n"
                + "common, uncommon, rare, or epic?");
        chatters.add(pl.getUniqueId());

        // save data file
        try {
            CHESTS_CONFIG.save(CHESTS);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @EventHandler
    public void onLootChestInventoryClick(InventoryClickEvent e) {
        if (e.getClickedInventory() == null) return;
        if (!(e.getView().getTopInventory().getHolder() instanceof LootChestInventory)) return;
        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(e.getCurrentItem());
        if (RunicItemsAPI.containsBlockedTag(runicItem))
            e.setCancelled(true);
    }
}
