package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

public class ClericTreeUtil {

    public static Perk DEFAULT_CLERIC_SPELL_PERK = new PerkSpell(996, 0, 1, 1, "Cleric");

    public static List<Perk> bardPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(36, 1, 0, 1, "Smoke Bomb"));
        perks.add(new PerkBaseStat(37, 1, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkSpell(38, 1, 0, 1, "Backstab"));
        perks.add(new PerkBaseStat(39, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(40, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(41, 1, 0, 1, "Cloak"));
        perks.add(new PerkBaseStat(42, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(43, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(44, 1, 0, 1, "Slice and Dice"));
        perks.add(new PerkBaseStat(45, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(46, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(47, 1, 0, 1, "Predator"));
        return perks;
    }

    public static List<Perk> exemplarList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(48, 1, 0, 1, "Smoke Bomb"));
        perks.add(new PerkBaseStat(49, 1, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkSpell(50, 1, 0, 1, "Backstab"));
        perks.add(new PerkBaseStat(51, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(52, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(53, 1, 0, 1, "Cloak"));
        perks.add(new PerkBaseStat(54, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(55, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(56, 1, 0, 1, "Slice and Dice"));
        perks.add(new PerkBaseStat(57, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(58, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(59, 1, 0, 1, "Predator"));
        return perks;
    }

    public static List<Perk> priestList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(60, 1, 0, 1, "Smoke Bomb"));
        perks.add(new PerkBaseStat(61, 1, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkSpell(62, 1, 0, 1, "Backstab"));
        perks.add(new PerkBaseStat(63, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(64, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(65, 1, 0, 1, "Cloak"));
        perks.add(new PerkBaseStat(66, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(67, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(68, 1, 0, 1, "Slice and Dice"));
        perks.add(new PerkBaseStat(69, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(70, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(71, 1, 0, 1, "Predator"));
        return perks;
    }
}
