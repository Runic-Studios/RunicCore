package com.runicrealms.plugin.utilities;

import com.nametagedit.plugin.NametagEdit;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NametagUtil {

    public static void updateNametag(Player pl) {
        String levelColor = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassLevel() >= PlayerLevelUtil.getMaxLevel()? "&6" : "&a";
        String nameColor = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getIsOutlaw() ? "&4" : "&r";
        String classPrefix = RunicCore.getCacheManager().getPlayerCache(pl.getUniqueId()).getClassName().substring(0, 2);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(),
                () -> NametagEdit.getApi().setPrefix(pl, levelColor + "[" + classPrefix + "|" + pl.getLevel() + "] " + nameColor), 1L);
    }
}
