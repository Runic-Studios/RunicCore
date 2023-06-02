package com.runicrealms.plugin.donator;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;

@CommandAlias("donor|donator")
public class DonorCommand extends BaseCommand {

    @Default
    @CatchUnknown
    public void onCommand(Player player) {
        player.openInventory(new DonorUI(player).getInventory());
    }

}
