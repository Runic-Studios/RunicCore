package com.runicrealms.plugin.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("mana")
public class ManaCMD extends BaseCommand{

    @Default
    @CatchUnknown
    @Conditions("is-op")
    public void onCommand(Player player) {
        int maxMana = RunicCore.getCacheManager().getPlayerCaches().get(player).getMaxMana();
        RunicCore.getRegenManager().getCurrentManaList().put(player.getUniqueId(), maxMana);
        player.sendMessage(ChatColor.AQUA + "You've restored your mana!");
    }
}
