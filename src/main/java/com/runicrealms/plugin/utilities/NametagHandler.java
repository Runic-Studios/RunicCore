package com.runicrealms.plugin.utilities;

import com.nametagedit.plugin.NametagEdit;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.event.NameTagEvent;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class NametagHandler implements Listener {

    /**
     * Calls a new name tag event
     *
     * @param player to update
     * @param slot   of the character
     */
    public static void updateNametag(Player player, int slot) {
        ChatColor prefixColor = player.getLevel() >= PlayerLevelUtil.getMaxLevel() ? ChatColor.GOLD : ChatColor.GREEN;
        String classPrefix = RunicCore.getCharacterAPI().getPlayerClass(player).substring(0, 2);
        // Call task sync
        Bukkit.getScheduler().runTask(RunicCore.getInstance(),
                () -> {
                    Bukkit.getPluginManager().callEvent(new NameTagEvent
                            (
                                    player,
                                    slot,
                                    prefixColor,
                                    ChatColor.RESET,
                                    "[" + classPrefix + "|" + player.getLevel() + "]"
                            ));
                });
    }

    @EventHandler(priority = EventPriority.HIGHEST) // Last thing to run
    public void onNameTagEvent(NameTagEvent event) {
        if (event.isCancelled()) return;
        final String finalName = event.getPrefixColor() + event.getNametag() + event.getNameColor();
        NametagEdit.getApi().setPrefix(event.getPlayer(), finalName + " ");
    }
}
