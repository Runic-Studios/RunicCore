package com.runicrealms.plugin.spellapi.skilltrees.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

@CommandAlias("resettree")
public class ResetTreeCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @Conditions("is-op")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(Player player, String[] args) {
        if (args.length == 0) {
            SkillTree.resetTree(player);
            return;
        }
        try {
            Player toReset = Bukkit.getPlayer(args[0]);
            SkillTree.resetTree(toReset);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "Player not found!");
        }
    }
}
