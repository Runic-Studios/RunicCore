package com.runicrealms.plugin.group;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.entity.Player;

@CommandAlias("group")
@Conditions("is-player")
public class GroupCommand extends BaseCommand {

    @Default
    @CatchUnknown
    @Subcommand("finder")
    @CommandCompletion("@nothing")
    public void onCommandFinder(Player player) {
        if (!RunicCore.getPartyManager().canJoinParty(player)) {
            player.sendMessage(ColorUtil.format("&r&cYou are already in a party or queue!"));
            return;
        }

        player.closeInventory();
        player.openInventory(RunicCore.getGroupManager().getUI().getInventory());
    }


    @Subcommand("leave")
    @CommandCompletion("@nothing")
    public void onCommandLeave(Player player) {
        if (!RunicCore.getGroupManager().isInQueue(player)) {
            player.sendMessage(ColorUtil.format("&r&cYou are not currently in a queue!"));
            return;
        }

        RunicCore.getGroupManager().removeFromQueue(player);
        player.sendMessage(ColorUtil.format("&r&aYou have left the queue!"));
    }
}
