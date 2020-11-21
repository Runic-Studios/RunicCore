package com.runicrealms.plugin.spellapi.skilltrees;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkillTree {

    private List<Perk> perks;

    public SkillTree() {
        perks = new ArrayList<>();
        perks.add(new PerkSpell(1, 0, 0, 1, "Meteor Shower"));
    }

    public SkillTree(Player player) {
        // generate default tree for player class
        // update perks w/ information
    }
}
