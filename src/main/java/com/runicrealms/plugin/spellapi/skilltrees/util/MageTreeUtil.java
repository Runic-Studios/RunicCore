package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.plugin.spellapi.skilltrees.SkillTree;

import java.util.List;

/**
 * Stores all default skill trees for the Mage class
 * @author Skyfallin
 */
public class MageTreeUtil {

    public static SkillTree pyromancerSkillTree() {
        SkillTree pyroSkillTree = new SkillTree();
        List<Perk> perks = pyroSkillTree.getPerks();
        perks.add(new PerkSpell(1, 0, 0, 1, "Fire Aura"));
        perks.add(new PerkBaseStat(1, 0, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(1, 0, 0, 1, "Fire Blast"));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(1, 0, 0, 1, "Meteor Shower"));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        return pyroSkillTree;
    }
}
