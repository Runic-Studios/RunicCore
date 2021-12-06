package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicitems.RunicItemsAPI;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.UUID;

public class BossChest {

    private final UUID bossUuid;
    private final Chest chest;
    private final DungeonLocation dungeonLocation;

    public BossChest(UUID bossUuid, Chest chest, DungeonLocation dungeonLocation) {
        this.bossUuid = bossUuid;
        this.dungeonLocation = dungeonLocation;
        this.chest = chest;
        addTokens();
//        fillChestWithDrops();
    }

    private void addTokens() {
        int numberOfBossSlayers = RunicCore.getBossTagger().getBossLooters(bossUuid).size();
        Inventory inventory = chest.getBlockInventory();
        inventory.setItem
                (
                        0,
                        RunicItemsAPI.generateItemFromTemplate(dungeonLocation.getCurrencyTemplateId(), numberOfBossSlayers).generateItem()
                );
    }

//    public static WeightedRandomBag<ItemStack> fillChestWithDrops() {
//        WeightedRandomBag<ItemStack> bossLootTable = new WeightedRandomBag<>();
//        LootChestRarity epic = LootChestRarity.EPIC;
//        // todo: get keys for drops, every key is a template id
//        // todo: token
////        ItemStack pufferfish = runicItem("Pufferfish", 2, 3);
//
//        // add entries to table
//        bossLootTable.addEntry(randomArmorOrWeaponInLevelRange, 30.0);
//        bossLootTable.addEntry(coin, 50.0);
//        return bossLootTable;
//    }

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
}
