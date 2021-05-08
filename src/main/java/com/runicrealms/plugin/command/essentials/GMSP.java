package com.runicrealms.plugin.command.essentials;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

@CommandAlias("gmsp|gm2")
public class GMSP extends BaseCommand {
    @Default
    @CatchUnknown
    @Conditions("is-op")
    public void onCommandCreative(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
    }
}
