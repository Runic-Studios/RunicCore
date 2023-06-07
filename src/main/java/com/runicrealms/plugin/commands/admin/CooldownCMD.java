package com.runicrealms.plugin.commands.admin;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.spellapi.spelltypes.Spell;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

@CommandAlias("cooldown|cd")
@CommandPermission("runic.op")
public class CooldownCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @Conditions("is-op")
    public void onCommand(Player player) {
        ConcurrentHashMap.KeySetView<Spell, Long> spells = RunicCore.getSpellAPI().getSpellsOnCooldown(player.getUniqueId());
        if (spells == null) return;
        for (Spell spell : spells) {
            RunicCore.getSpellAPI().reduceCooldown(player, spell, 999999);
        }
        player.sendMessage(ChatColor.GREEN + "You've reset your cooldowns!");
    }

}
