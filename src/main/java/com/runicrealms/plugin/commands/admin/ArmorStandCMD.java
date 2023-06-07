package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@CommandAlias("armorstand")
@CommandPermission("runic.op")
public class ArmorStandCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @Conditions("is-op")
    public void onCommand(Player player) {
        for (Entity entity : player.getNearbyEntities(5, 5, 5)) {
            if (!(entity instanceof ArmorStand)) continue;
            entity.remove();
        }
    }
}
