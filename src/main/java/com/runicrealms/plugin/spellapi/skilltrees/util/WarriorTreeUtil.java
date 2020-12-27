package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

public class WarriorTreeUtil {

    public static Perk DEFAULT_WARRIOR_SPELL_PERK = new PerkSpell(997, 0, 1, 1, "Slam");

    public static List<Perk> berserkerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(144, 1, 0, 1, "Enrage"));
        perks.add(new PerkBaseStat(145, 1, 0, 5, BaseStatEnum.STRENGTH, 1));
        perks.add(new PerkSpell(146, 1, 0, 1, "Cleave")); // during enrage, your melee attacks cleave enemies
        perks.add(new PerkBaseStat(147, 1, 0, 3, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkBaseStat(148, 1, 0, 3, BaseStatEnum.STRENGTH, 1));
        perks.add(new PerkSpell(149, 1, 0, 1, "Leech"));
        perks.add(new PerkBaseStat(150, 1, 0, 3, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkBaseStat(151, 1, 0, 3, BaseStatEnum.STRENGTH, 1));
        perks.add(new PerkBaseStat(152, 1, 0, 5, BaseStatEnum.DEXTERITY, 1));
        perks.add(new PerkSpell(153, 1, 0, 1, "Whirlwind"));
        perks.add(new PerkBaseStat(154, 1, 0, 3, BaseStatEnum.STRENGTH, 1));
        perks.add(new PerkSpell(155, 1, 0, 1, "Last Resort")); // upon death, revive with X% health. cannot occur >1 every Ys
        return perks;
    }

    public static List<Perk> guardianPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(156, 1, 0, 1, "Rescue")); // throw your axe and pull ally to you, granting them X% hp shield
        perks.add(new PerkBaseStat(157, 1, 0, 5, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkSpell(158, 1, 0, 1, "Taunt")); // on-hit threat
        perks.add(new PerkBaseStat(159, 1, 0, 3, BaseStatEnum.STRENGTH, 1));
        perks.add(new PerkBaseStat(160, 1, 0, 3, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkSpell(161, 1, 0, 1, "Ray of Light")); // fire vector beam, teleport to first ally hit, granting you and them X% dmg reduction
        perks.add(new PerkBaseStat(162, 1, 0, 3, BaseStatEnum.STRENGTH, 1));
        perks.add(new PerkBaseStat(163, 1, 0, 3, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkBaseStat(164, 1, 0, 5, BaseStatEnum.STRENGTH, 1));
        perks.add(new PerkSpell(165, 1, 0, 1, "Judgment")); // add flat shield
        perks.add(new PerkBaseStat(166, 1, 0, 3, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkSpell(167, 1, 0, 1, "Resolve"));
        return perks;
    }

    public static List<Perk> inquisitorPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(168, 1, 0, 1, "Throw Axe")); // silence
        perks.add(new PerkBaseStat(169, 1, 0, 5, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkSpell(170, 1, 0, 1, "Condemn")); // deal X% more damage to silenced enemies
        perks.add(new PerkBaseStat(171, 1, 0, 3, BaseStatEnum.STRENGTH, 3));
        perks.add(new PerkBaseStat(172, 1, 0, 3, BaseStatEnum.VITALITY, 3));
        perks.add(new PerkSpell(173, 1, 0, 1, "Rebuke")); // slow/dmg then mark to take X% more damage from all sources
        perks.add(new PerkBaseStat(174, 1, 0, 3, BaseStatEnum.STRENGTH, 3));
        perks.add(new PerkBaseStat(175, 1, 0, 3, BaseStatEnum.VITALITY, 5));
        perks.add(new PerkSpell(176, 1, 0, 1, "Rift")); // vaccuum-like portal that draws in enemies
        perks.add(new PerkBaseStat(177, 1, 0, 3, BaseStatEnum.STRENGTH, 3));
        perks.add(new PerkBaseStat(178, 1, 0, 3, BaseStatEnum.VITALITY, 3));
        perks.add(new PerkSpell(179, 1, 0, 1, "Subdue")); // melee attacks have X% chance to silence
        return perks;
    }
}
