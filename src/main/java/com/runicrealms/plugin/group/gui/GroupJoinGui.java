package com.runicrealms.plugin.group.gui;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.group.Group;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GroupJoinGui implements Listener {

    private static Map<Player, Integer> viewers = new HashMap<Player, Integer>();

    public static void display(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Join a Group");
        int page = viewers.containsKey(player) ? viewers.get(player) : 0;
        int slot = 9;
        Iterator<Group> iterator = RunicCore.getGroupManager().getGroups().iterator();
        int count = 0;
        while (iterator.hasNext()) {
            if (count >= page * 45 && count < RunicCore.getGroupManager().getGroups().size() - page * 45) {
                inventory.setItem(slot, iterator.next().getIcon());
                slot++;
            } else {
                iterator.next();
            }
            count++;
        }
        player.openInventory(inventory);
        viewers.put(player, page);
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
