package com.runicrealms.plugin.spellapi.skilltrees;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkillTree {

    private List<Perk> perks;

    public SkillTree() {
        perks = new ArrayList<>();
        perks.add(new PerkSpell(1, 0, 0, 1, "Meteor Shower"));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
    }

    public SkillTree(Player player) {
        // generate default tree for player class
        // update perks w/ information
    }

    public List<Perk> getPerks() {
        return perks;
    }
}
