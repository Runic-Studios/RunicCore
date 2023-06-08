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

import java.util.Map;

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
        Map<SkillTreePosition, SkillTreeData> skillTreeDataMap = RunicCore.getSkillTreeAPI().getSkillTreeDataMap(target.getUniqueId(), slot);
        String message = ColorUtil.format
                ("&e&l" + target.getName() + "s Profile" +
                        "\n&7Class: &f" + className + " &7lv. " + classLevel);
        if (skillTreeDataMap.get(SkillTreePosition.FIRST) != null) {
            SkillTreeData first = skillTreeDataMap.get(SkillTreePosition.FIRST);
            message += ChatColor.GRAY + "\nSubclass: " + first.getSubClass(target.getUniqueId()).getName() + " " + ChatColor.WHITE + first.getTotalPoints();
        }
        if (skillTreeDataMap.get(SkillTreePosition.SECOND) != null) {
            SkillTreeData second = skillTreeDataMap.get(SkillTreePosition.SECOND);
            message += ChatColor.GRAY + ", " + second.getSubClass(target.getUniqueId()).getName() + " " + ChatColor.WHITE + second.getTotalPoints();
        }
        if (skillTreeDataMap.get(SkillTreePosition.THIRD) != null) {
            SkillTreeData third = skillTreeDataMap.get(SkillTreePosition.THIRD);
            message += ChatColor.GRAY + ", " + third.getSubClass(target.getUniqueId()).getName() + " " + ChatColor.WHITE + third.getTotalPoints();
        }
        player.sendMessage("");
        player.sendMessage(message);
        player.sendMessage("");
    }

}
