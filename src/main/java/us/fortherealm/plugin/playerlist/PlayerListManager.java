package us.fortherealm.plugin.playerlist;

import io.puharesource.mc.titlemanager.api.v2.TitleManagerAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import us.fortherealm.plugin.Main;

public class TabManager implements Listener {

    private Plugin plugin = Main.getInstance();
    TitleManagerAPI api = (TitleManagerAPI) Bukkit.getServer().getPluginManager().getPlugin("TitleManager");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)

    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

//        for (int i = 0; i < 80; i++) {
//            ProtocolTabAPI.getTablist(player).setSlot(i, ProtocolTab.BLANK_TEXT);
//        }
//
//        ProtocolTabAPI.getTablist(player).setHeader("&cmd_5 is love!");
//        ProtocolTabAPI.getTablist(player).setHeader("&6md_5 is life!");
//
//        ProtocolTabAPI.getTablist(player).setSlot(0, "First slot.");
//        ProtocolTabAPI.getTablist(player).setSlot(1, "&eSecond slot.");
//
//        // grab the player's stored name
//        // convert it to a string
//        Object storedName = plugin.getConfig().get(player.getUniqueId() + ".info.name");
//        String nameToString = storedName.toString();
//
//        ProtocolTabAPI.getTablist(player).setSlot(2, nameToString);
//
//
//
//        ProtocolTabAPI.getTablist(player).update();


        api.



    }
}
