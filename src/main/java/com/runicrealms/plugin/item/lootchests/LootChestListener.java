package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.item.GUIMenu.ItemGUI;
import com.runicrealms.plugin.item.GUIMenu.OptionClickEvent;
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
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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

    @EventHandler(priority = EventPriority.NORMAL) // first
    public void onChestInteract(PlayerInteractEvent e) {

        if (!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!(e.getHand() == EquipmentSlot.HAND)) return;
        if (!e.hasBlock()) return;
        if (e.getClickedBlock() == null) return;
        if (e.getClickedBlock().getType() != Material.CHEST) return;
        Chest chest = (Chest) e.getClickedBlock().getState();
        BossChest bossChest = BossChest.getFromBlock(RunicCore.getBossTagger().getActiveBossLootChests(), chest);
        if (bossChest != null) return; // handled in BossTagger
        e.setCancelled(true);

        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        Location blockLoc = block.getLocation();
        if (block.getType() == Material.AIR) return;

        if (RunicCore.getLootChestManager().getQueuedChest(blockLoc) != null) return; // chest already queued
        if (RunicCore.getLootChestManager().getLootChest(blockLoc) == null)
            return; // chest doesn't match saved chest locations
        LootChest lootChest = RunicCore.getLootChestManager().getLootChest(blockLoc);

        /*
        iterate through data file, if it finds a saved station w/ same world, x, y, and z, then
        it checks 'tier' and switch statement to spawn correct loot
        if we've found a location and a chest, open the associated ItemGUI
         */
        LootChestRarity lootChestRarity = lootChest.getLootChestRarity();

        // verify player level
        if (player.getLevel() < lootChestRarity.getMinAccessLevel()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You must be at least level " + lootChestRarity.getMinAccessLevel() + " to open this.");
            return;
        }

        // destroy chest, open inv if all conditions are met
        RunicCore.getLootChestManager().getQueuedChests().put(lootChest, System.currentTimeMillis()); // update respawn timer
        player.getWorld().playSound(blockLoc, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1f, 1);
        block.setType(Material.AIR);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        openChestGUI(player, lootChestRarity);
    }

    /**
     * Command to generate the chest loot for player
     *
     * @param player          player who opened chest
     * @param lootChestRarity rarity of loot chest
     */
    private void openChestGUI(Player player, LootChestRarity lootChestRarity) {

        Random rand = new Random();
        ItemGUI chest;
        ItemStack chestItem = new ItemStack(Material.AIR);
        String chestName = "";
        int minItems = 0;
        int maxItems = 0;

        // check which stationType is being called
        switch (lootChestRarity) {
            case COMMON:
                chestName = "&f&l" + player.getName() + "'s &7&lCommon Chest";
                minItems = 2;
                maxItems = 4;
                break;
            case UNCOMMON:
                chestName = "&f&l" + player.getName() + "'s &a&lUncommon Chest";
                minItems = 2;
                maxItems = 4;
                break;
            case RARE:
                chestName = "&f&l" + player.getName() + "'s &b&lRare Chest";
                minItems = 3;
                maxItems = 5;
                break;
            case EPIC:
                chestName = "&f&l" + player.getName() + "'s &d&lEpic Chest";
                minItems = 3;
                maxItems = 5;
                break;
        }

        // create chest inventory
        chest = new ItemGUI(chestName, 27, (OptionClickEvent event) -> {
        }, RunicCore.getInstance());
        int numOfItems = rand.nextInt(maxItems - minItems) + minItems;

        List<Integer> used = new ArrayList<>();
        for (int i = 0; i < numOfItems; i++) {

            // prevent items overriding the same slot
            int slot = rand.nextInt(26);
            while (used.contains(slot)) {
                slot = rand.nextInt(26);
            }
            if (!used.contains(slot)) {
                used.add(slot);
            }

            // fill inventory
            switch (lootChestRarity) {
                case COMMON:
                    chestItem = ChestLootTableUtil.commonLootTable().getRandom();
                    break;
                case UNCOMMON:
                    chestItem = ChestLootTableUtil.uncommonLootTable().getRandom();
                    break;
                case RARE:
                    chestItem = ChestLootTableUtil.rareLootTable().getRandom();
                    break;
                case EPIC:
                    chestItem = ChestLootTableUtil.epicLootTable().getRandom();
                    break;
            }
            chest.setOption(slot, chestItem);
        }
        chest.open(player);
    }

    /**
     * This event adds a new workstation to the file, so long as the player is opped and holding a green wool.
     * The event then listens for the player's chat response, and adds the block to the file accordingly.
     * NEW: If the player is holding red wool, they can remove a chest.
     */
    private final ArrayList<UUID> chatters = new ArrayList<>();

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
}
