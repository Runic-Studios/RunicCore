package com.runicrealms.plugin.model;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.TitleAPI;
import com.runicrealms.plugin.rdb.api.WriteCallback;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.util.UUID;

public class TitleManager implements Listener, TitleAPI {

    public TitleManager() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
    }

    @Override
    public TitleData getTitleData(UUID uuid) {
        // Step 1: Check if title data is memoized
        CorePlayerData corePlayerData = RunicCore.getPlayerDataAPI().getCorePlayerData(uuid);
        if (corePlayerData != null) {
            return corePlayerData.getTitleData();
        }
        return null; // Oh-no!
    }

    @Override
    public void removePrefixesAndSuffixes(Player player, WriteCallback callback) {
        UUID uuid = player.getUniqueId();
        TitleData titleData = getTitleData(player.getUniqueId());
        titleData.setSuffix("");
        titleData.setPrefix("");
        RunicCore.getCoreWriteOperation().updateCorePlayerData
                (
                        uuid,
                        "titleData",
                        titleData,
                        () -> {
                            callback.onWriteComplete();
                            player.sendMessage(ChatColor.YELLOW + "Your titles have been reset!");
                        }
                );
    }

}
