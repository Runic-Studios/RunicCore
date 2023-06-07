package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.player.listener.ManaListener;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("mana")
@CommandPermission("runic.op")
public class ManaCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @Conditions("is-op")
    public void onCommand(Player player) {
        int maxMana = ManaListener.calculateMaxMana(player);
        RunicCore.getRegenManager().getCurrentManaList().put(player.getUniqueId(), maxMana);
        player.sendMessage(ChatColor.AQUA + "You've restored your mana!");
    }
}
