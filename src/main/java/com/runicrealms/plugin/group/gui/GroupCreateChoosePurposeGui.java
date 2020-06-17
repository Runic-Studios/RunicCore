package com.runicrealms.plugin.group.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;

public class GroupCreateChoosePurposeGui implements Listener {

    private static Set<Player> viewers = new HashSet<Player>();

    public static void display(Player player) {

    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

    }

}
