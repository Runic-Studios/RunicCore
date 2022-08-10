package com.runicrealms.plugin.utilities;

import com.nametagedit.plugin.NametagEdit;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.api.RunicCoreAPI;
import com.runicrealms.plugin.model.CharacterField;
import com.runicrealms.plugin.player.utilities.PlayerLevelUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class NametagUtil {

    public static void updateNametag(Player player) {
        String levelColor = player.getLevel() >= PlayerLevelUtil.getMaxLevel() ? "&6" : "&a";
        int slot = RunicCoreAPI.getCharacterSlot(player.getUniqueId());
        boolean isOutlaw = Boolean.parseBoolean(RunicCoreAPI.getRedisCharacterValue(player.getUniqueId(), CharacterField.OUTLAW_ENABLED.getField(), slot));
        String nameColor = isOutlaw ? "&4" : "&r";
        String classPrefix = RunicCoreAPI.getPlayerClass(player).substring(0, 2);
        Bukkit.getScheduler().scheduleSyncDelayedTask(RunicCore.getInstance(),
                () -> NametagEdit.getApi().setPrefix(player, levelColor + "[" + classPrefix + "|" + player.getLevel() + "] " + nameColor), 1L);
    }
}
