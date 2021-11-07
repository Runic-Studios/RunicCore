package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.spellapi.PlayerSpellWrapper;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.runicitems.Stat;

import java.util.ArrayList;
import java.util.List;

public class WarriorTreeUtil {

    public static Perk DEFAULT_WARRIOR_SPELL_PERK = new PerkSpell(997, 0, 1, 1, PlayerSpellWrapper.DEFAULT_WARRIOR);

    public static List<Perk> berserkerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(144, 1, 0, 1, "Enrage"));
        perks.add(new PerkBaseStat(145, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(146, 1, 0, 1, "Cleave"));
        perks.add(new PerkBaseStat(147, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(148, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(149, 1, 0, 1, "Leech"));
        perks.add(new PerkBaseStat(150, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(151, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(152, 1, 0, 5, Stat.VITALITY));
        perks.add(new PerkSpell(153, 1, 0, 1, "Whirlwind"));
        perks.add(new PerkBaseStat(154, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(155, 1, 0, 1, "Last Resort"));
        return perks;
    }

    public static List<Perk> guardianPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(156, 1, 0, 1, "Rescue"));
        perks.add(new PerkBaseStat(157, 1, 0, 5, Stat.VITALITY));
        perks.add(new PerkSpell(158, 1, 0, 1, "Taunt"));
        perks.add(new PerkBaseStat(159, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(160, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkSpell(161, 1, 0, 1, "Bolster"));
        perks.add(new PerkBaseStat(162, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(163, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(164, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(165, 1, 0, 1, "Judgment"));
        perks.add(new PerkBaseStat(166, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkSpell(167, 1, 0, 1, "Resolve"));
        return perks;
    }

    public static List<Perk> inquisitorPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(168, 1, 0, 1, "Throw Axe"));
        perks.add(new PerkBaseStat(169, 1, 0, 5, Stat.VITALITY));
        perks.add(new PerkSpell(170, 1, 0, 1, "Condemn"));
        perks.add(new PerkBaseStat(171, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(172, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkSpell(173, 1, 0, 1, "Rebuke"));
        perks.add(new PerkBaseStat(174, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(175, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(177, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(176, 1, 0, 1, "Rift"));
        perks.add(new PerkBaseStat(178, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkSpell(179, 1, 0, 1, "Subdue"));
        return perks;
    }
}
