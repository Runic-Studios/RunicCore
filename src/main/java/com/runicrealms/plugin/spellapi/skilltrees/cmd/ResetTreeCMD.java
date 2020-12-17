package com.runicrealms.plugin.spellapi.skilltrees.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;
import org.bukkit.entity.Player;

@CommandAlias("resettree")
public class ResetTreeCMD extends BaseCommand {

    @Default
    @CatchUnknown
    @Conditions("is-op")
    @Syntax("<player>")
    @CommandCompletion("@players")
    public void onCommand(Player player, Player toReset) {
        SkillTree.resetTree(toReset);
    }
}
