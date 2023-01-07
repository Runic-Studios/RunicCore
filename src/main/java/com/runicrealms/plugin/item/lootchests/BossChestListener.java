package com.runicrealms.plugin.item.lootchests;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import com.runicrealms.runicitems.item.stats.RunicItemTag;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.UUID;

/**
 * Logic for boss chest block and inventory interact
 */
public class BossChestListener implements Listener {

    /**
     * Controls logic for when player clicks around in a boss chest inventory
     */
    @EventHandler
    public void onBossChestInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof BossChestInventory)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());
        if (runicItem.getTags().contains(RunicItemTag.SOULBOUND))
            event.setCancelled(true);
    }

    /**
     * Controls logic when player right-clicks a boss chest
     */
    @EventHandler(priority = EventPriority.LOWEST) // first
    public void onChestInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!(event.getHand() == EquipmentSlot.HAND)) return;
        if (!event.hasBlock()) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        Block block = event.getClickedBlock();
        Chest chest = (Chest) block.getState();
        BossChest bossChest = BossChest.getFromBlock(RunicCore.getBossTagger().getActiveBossLootChests(), chest);
        if (bossChest == null) return;
        UUID bossId = bossChest.getBossUuid();
        Player player = event.getPlayer();
        event.setCancelled(true);
        if (RunicCore.getBossTagger().getBossLooters(bossId).contains(player.getUniqueId())) {
            bossChest.attemptToOpen(player);
        } else {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1.0f);
            player.sendMessage(ChatColor.RED + "Only the slayers of the boss may open the spoils!");
        }
    }

}
