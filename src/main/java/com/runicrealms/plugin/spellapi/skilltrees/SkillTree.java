package com.runicrealms.plugin.spellapi.skilltrees;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SkillTree {

    private List<Perk> perks;

    public SkillTree() {
        perks = new ArrayList<>();
    }

    public SkillTree(Player player) {
        // generate default tree for player class
        // update perks w/ information
    }

    public List<Perk> getPerks() {
        return perks;
    }
}
