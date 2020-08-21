package com.runicrealms.plugin.player.outlaw;

import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.NametagUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetOutlawCMD implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        // outlaw <player.name>
        if (!sender.isOp())
            return true;

        Player pl = Bukkit.getPlayer(args[0]);
        if (pl == null)
            return true;

        // toggle their current outlaw status, set thier rating to default
        RunicCore.getCacheManager().getPlayerCaches().get(pl).setOutlaw(!OutlawManager.isOutlaw(pl));
        RunicCore.getCacheManager().getPlayerCaches().get(pl).setRating(RunicCore.getOutlawManager().getBaseRating());

        NametagUtil.updateNametag(pl);
        return true;
    }
}