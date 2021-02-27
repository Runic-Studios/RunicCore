package com.runicrealms.plugin.spellapi.skilltrees.cmd;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Default;
import org.bukkit.entity.Player;

@CommandAlias("togglespellui|togglespelltips|togglespellhud")
public class ToggleSpellUICMD extends BaseCommand {

    @Default
    @CatchUnknown
    @CommandCompletion("@players")
    public void onCommand(Player player) {
//        if (!character.has(SkillTree.PATH_LOCATION + "." + SkillTree.SPELLS_LOCATION))) {
//
//        } else {
//
//        }
    }
}
