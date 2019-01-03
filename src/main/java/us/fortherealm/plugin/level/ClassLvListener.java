package us.fortherealm.plugin.level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import us.fortherealm.plugin.Main;

public class ClassLvListener implements Listener {

    @EventHandler
    public void onLevelUp(PlayerLevelChangeEvent e) {

        Player pl = e.getPlayer();

        // grab all the player's info
        String className = Main.getInstance().getConfig().getString(pl.getUniqueId() + ".info.class.name");
        //int classLevel = Main.getInstance().getConfig().getInt(pl.getUniqueId() + ".info.class.level");

        pl.sendTitle(
                ChatColor.GREEN + "Level Up!",
                ChatColor.GREEN + className + pl.getLevel() + "!", 10, 40, 10);
    }
}
