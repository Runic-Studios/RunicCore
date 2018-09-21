package me.skyfallin.plugin.listeners;

import me.skyfallin.plugin.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class HealthScaleListener implements Listener {

    private Main plugin = Main.getPlugin(Main.class);

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        e.getPlayer().setHealthScale((player.getMaxHealth() / 12.5));//ex: (50 / 12.5) = 4.0 = 2 hearts
    }
}
