package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.utilities.ConfigUtil;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class BossChest {

    private final UUID bossUuid;
    private final Chest chest;
    private final DungeonLocation dungeonLocation;
    private final HashSet<UUID> playersWhoHaveReceivedTokens;

    public BossChest(UUID bossUuid, Chest chest, DungeonLocation dungeonLocation) {
        this.bossUuid = bossUuid;
        this.dungeonLocation = dungeonLocation;
        this.chest = chest;
        this.playersWhoHaveReceivedTokens = new HashSet<>();
        populateChestWithBossDrops();
    }

    /**
     * First checks if the player's inventory has a free space, then adds a token directly to inventory
     * Then, opens the chest inventory with boss drops
     *
     * @param player who wants to open chest
     */
    public void attemptToOpen(Player player) {
        if (!playersWhoHaveReceivedTokens.contains(player.getUniqueId())) {
            if (player.getInventory().firstEmpty() == -1) {
                player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
                player.sendMessage(ChatColor.RED + "You must have at least one empty inventory space!");
                return;
            }
            playersWhoHaveReceivedTokens.add(player.getUniqueId());
            player.sendMessage(ChatColor.GREEN + "You received a dungeon token!");
            RunicItemsAPI.addItem
                    (
                            player.getInventory(),
                            RunicItemsAPI.generateItemFromTemplate(dungeonLocation.getCurrencyTemplateId()).generateItem()
                    );
        }
        player.openInventory(chest.getBlockInventory());
    }

    /**
     * ?
     */
    private void populateChestWithBossDrops() {
        ConfigurationSection dungeonSection = ConfigUtil.getDungeonConfigurationSection().getConfigurationSection(dungeonLocation.getIdentifier());
        if (dungeonSection == null) return;
        ConfigurationSection drops = dungeonSection.getConfigurationSection("drops");
        if (drops == null) return;
        Set<String> dropsSection = drops.getKeys(false);
        Set<Integer> used = new HashSet<>();
        for (String drop : dropsSection) {
            // prevent items overriding the same slot
            int slot = ThreadLocalRandom.current().nextInt(0, 27);
            while (used.contains(slot)) {
                slot = ThreadLocalRandom.current().nextInt(0, 27);
            }
            used.add(slot);
            double chance = drops.getDouble(drop + ".chance");
            if (ThreadLocalRandom.current().nextDouble() <= chance) {
                int amount = drops.getInt(drop + ".amount");
                chest.getBlockInventory().setItem
                        (
                                slot,
                                RunicItemsAPI.generateItemFromTemplate(drop, amount).generateItem()
                        );
            }
        }
    }

    /**
     * @param activeBossLootChests
     * @param chest
     * @return
     */
    public static BossChest getFromBlock(HashMap<UUID, BossChest> activeBossLootChests, Chest chest) {
        for (BossChest bossChest : activeBossLootChests.values()) {
            if (bossChest.getChest().equals(chest))
                return bossChest;
        }
        return null;
    }

    public UUID getBossUuid() {
        return bossUuid;
    }

    public Chest getChest() {
        return chest;
    }

    public DungeonLocation getDungeonLocation() {
        return dungeonLocation;
    }

    public HashSet<UUID> getPlayersWhoHaveReceivedTokens() {
        return playersWhoHaveReceivedTokens;
    }
}
