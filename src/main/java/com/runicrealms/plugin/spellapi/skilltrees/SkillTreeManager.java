package com.runicrealms.plugin.spellapi.skilltrees;

import java.util.HashSet;

public class SkillTreeManager {

    private final HashSet<SkillTree> skillTrees;

    public SkillTreeManager() {
        skillTrees = new HashSet<>();
    }

    public void saveSkillTrees(boolean async) {
        for (SkillTree skillTree : skillTrees) {
            skillTree.save(async);
        }
    }

    public HashSet<SkillTree> getSkillTrees() {
        return skillTrees;
    }
}
