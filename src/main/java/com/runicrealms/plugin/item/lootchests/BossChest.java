package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.DungeonLocation;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

/**
 * Custom object implementation for the chest which spawns upon defeating a boss
 */
public class BossChest {
    private final UUID bossUuid;
    private final Chest chest;
    private final DungeonLocation dungeonLocation;
    private final HashMap<UUID, BossChestInventory> playerBossChestMap;

    /**
     * @param bossUuid        of the mob entity
     * @param chest           that spawns on boss death
     * @param dungeonLocation the enum value of which dungeon
     */
    public BossChest(UUID bossUuid, Chest chest, DungeonLocation dungeonLocation) {
        this.bossUuid = bossUuid;
        this.dungeonLocation = dungeonLocation;
        this.chest = chest;
        this.playerBossChestMap = new HashMap<>();
    }

    /**
     * Get the associated boss chest from the given chest block (if it exists)
     *
     * @param activeBossLootChests the map of all boss chests which are spawned
     * @param chest                the block to match (must be a chest)
     * @return a BossChest object
     */
    public static BossChest getFromBlock(HashMap<UUID, BossChest> activeBossLootChests, Chest chest) {
        for (BossChest bossChest : activeBossLootChests.values()) {
            if (bossChest.getChest().equals(chest))
                return bossChest;
        }
        return null;
    }

    /**
     * First checks if the player's inventory has a free space, then adds a token directly to inventory
     * Then, opens the chest inventory with boss drops
     *
     * @param player who wants to open chest
     */
    public void attemptToOpen(Player player) {
        // Handle first open of the chest
        if (!playerBossChestMap.containsKey(player.getUniqueId())) {
            playerBossChestMap.put(player.getUniqueId(), new BossChestInventory(player, dungeonLocation));
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1f, 0.1f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 0.5f);
        player.openInventory(playerBossChestMap.get(player.getUniqueId()).getInventory());
    }

    public UUID getBossUuid() {
        return bossUuid;
    }

    public Chest getChest() {
        return chest;
    }

}
