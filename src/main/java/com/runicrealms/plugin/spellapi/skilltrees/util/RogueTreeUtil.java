package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

public class RogueTreeUtil {

    public static List<Perk> assassinPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(0, 1, 0, 0, 1, "Smoke Bomb"));
        perks.add(new PerkBaseStat(1, 1, 0, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkSpell(2, 1, 0, 0, 1, "Backstab"));
        perks.add(new PerkBaseStat(3, 1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(4, 1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(5, 1, 0, 0, 1, "Cloak"));
        perks.add(new PerkBaseStat(6, 1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(7, 1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(8, 1, 0, 0, 1, "Slice and Dice"));
        perks.add(new PerkBaseStat(9, 1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(10, 1, 0, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(11, 1, 0, 0, 1, "Predator"));
        return perks;
    }
}
