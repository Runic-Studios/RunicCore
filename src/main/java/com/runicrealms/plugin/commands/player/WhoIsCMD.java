package com.runicrealms.plugin.commands.player;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Syntax;
import com.runicrealms.plugin.RunicCore;
import com.runicrealms.plugin.common.util.ColorUtil;
import com.runicrealms.plugin.model.SkillTreeData;
import com.runicrealms.plugin.model.SkillTreePosition;
import com.runicrealms.plugin.rdb.RunicDatabase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandAlias("whois")
public class WhoIsCMD extends BaseCommand {

    @CatchUnknown
    @CommandCompletion("@online")
    @Default
    @Syntax("<player>")
    public void onCommandClass(CommandSender commandSender, String[] args) {
        if (args.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "Error, incorrect number of arguments. Usage: whois {player}");
            return;
        }
        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage("Only a player may run this command!");
            return;
        }
        String playerName = args[0];
        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            commandSender.sendMessage(ChatColor.RED + "Error: player not found!");
            return;
        }
        String className = RunicDatabase.getAPI().getCharacterAPI().getPlayerClass(target);
        int classLevel = target.getLevel();
        int slot = RunicDatabase.getAPI().getCharacterAPI().getCharacterSlot(player.getUniqueId());
        // Skill Tree data to show subclass info
        SkillTreeData first = RunicCore.getSkillTreeAPI().loadSkillTreeData(target.getUniqueId(), slot, SkillTreePosition.FIRST);
        SkillTreeData second = RunicCore.getSkillTreeAPI().loadSkillTreeData(target.getUniqueId(), slot, SkillTreePosition.SECOND);
        SkillTreeData third = RunicCore.getSkillTreeAPI().loadSkillTreeData(target.getUniqueId(), slot, SkillTreePosition.THIRD);
        player.sendMessage("");
        player.sendMessage(ColorUtil.format
                ("&e&l" + player.getName() + "s Profile" +
                        "\n&7Class: &f" + className + " &7lv. " + classLevel +
                        "\n&7Subclass: &7" + first.getSubClass(target.getUniqueId()).getName() + " &f" + first.getTotalPoints() +
                        "&7, " + second.getSubClass(target.getUniqueId()).getName() + " &f" + second.getTotalPoints() +
                        "&7, " + third.getSubClass(target.getUniqueId()).getName() + " &f" + third.getTotalPoints()));
        player.sendMessage("");
    }

}
