package com.runicrealms.plugin.item.shops;

import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RunicShopManager implements Listener {

    private final Map<UUID, RunicShop> playersInShops = new HashMap<>();

    public RunicShopManager() {
        Bukkit.getPluginManager().registerEvents(new ScrapperListener(), RunicCore.getInstance());
    }

    public Map<UUID, RunicShop> getPlayersInShops() {
        return playersInShops;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        playersInShops.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        playersInShops.remove(event.getPlayer().getUniqueId());
    }
}
