package com.runicrealms.plugin.item;

import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.item.util.ItemRemover;
import com.runicrealms.runicitems.item.event.RunicItemGenericTriggerEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class TeleportScrollListener implements Listener {

    @EventHandler
    public void onTeleportScrollUse(RunicItemGenericTriggerEvent e) {
        if (RunicCoreAPI.isInCombat(e.getPlayer())) {
            e.getPlayer().sendMessage(ChatColor.RED + "You can't use that in combat!");
            return;
        }
        if (!e.getItem().getTriggers().get(e.getTrigger()).equals("teleport-scroll")) return;
        try {
            String world = e.getItem().getData().get("world");
            double x = Double.parseDouble(e.getItem().getData().get("x"));
            double y = Double.parseDouble(e.getItem().getData().get("y"));
            double z = Double.parseDouble(e.getItem().getData().get("z"));
            float yaw = Float.parseFloat(e.getItem().getData().get("yaw"));
            float pitch = Float.parseFloat(e.getItem().getData().get("pitch"));
            Location location = new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
            ItemRemover.takeItem(e.getPlayer(), e.getItemStack(), 1);
            RunicCoreAPI.beginTeleportation(e.getPlayer(), location);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            Bukkit.getLogger().info(ChatColor.RED + "Error: teleport scroll location is null.");
        }
    }
}
