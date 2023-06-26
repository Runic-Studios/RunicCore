package com.runicrealms.plugin.loot;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.rdb.event.CharacterLoadedEvent;
import com.runicrealms.plugin.rdb.event.CharacterQuitEvent;
import com.runicrealms.runicitems.RunicItemsAPI;
import com.runicrealms.runicitems.item.RunicItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LootChestOpenListener implements Listener {

    private final Map<UUID, ConcurrentHashMap<RegenerativeLootChest, Long>> lastOpened = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL) // first
    public void onChestInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!(event.getHand() == EquipmentSlot.HAND)) return;
        if (!event.hasBlock()) return;
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.CHEST) return;
        event.setCancelled(true);

        Map<RegenerativeLootChest, Long> playerOpened = lastOpened.get(event.getPlayer().getUniqueId());
        if (playerOpened == null) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        RegenerativeLootChest lootChest = RunicCore.getLootAPI().getRegenerativeLootChest(location);
        if (lootChest == null) return;

        if (player.getLevel() < lootChest.getMinLevel()) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You must be at least level " + lootChest.getMinLevel() + " to open this.");
            return;
        }

        // verify chest has not already been looted
        long lastOpenedTime = playerOpened.get(lootChest);
        int timeLeft = (int) ((lastOpenedTime + lootChest.getRegenerationTime() * 1000L - System.currentTimeMillis()) / 1000);
        if (timeLeft > 0) {
            player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 0.5f, 1);
            player.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " seconds to loot this chest again!");
            return;
        }

        playerOpened.put(lootChest, System.currentTimeMillis());
        player.getWorld().playSound(location, Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 0.1f, 1);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1);
        lootChest.openInventory(player);
    }

    @EventHandler
    public void onCharacterQuit(CharacterQuitEvent event) {
        lastOpened.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onCharacterLoaded(CharacterLoadedEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(RunicCore.getInstance(), () -> {
            ConcurrentHashMap<RegenerativeLootChest, Long> playerOpened = new ConcurrentHashMap<>();
            long time = System.currentTimeMillis();
            for (RegenerativeLootChest lootChest : RunicCore.getLootAPI().getRegenerativeLootChests()) {
                playerOpened.put(lootChest, time);
            }
            lastOpened.put(event.getPlayer().getUniqueId(), playerOpened);
        });
    }

    @EventHandler
    public void onLootChestInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;
        if (!(event.getView().getTopInventory().getHolder() instanceof LootChestInventory)) return;
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        RunicItem runicItem = RunicItemsAPI.getRunicItemFromItemStack(event.getCurrentItem());
        if (RunicItemsAPI.containsBlockedTag(runicItem))
            event.setCancelled(true);
    }
}
