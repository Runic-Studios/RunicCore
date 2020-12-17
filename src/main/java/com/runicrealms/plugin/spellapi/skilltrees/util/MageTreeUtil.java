package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all default perk lists for the Mage class
 * @author Skyfallin
 */
public class MageTreeUtil {

    public static List<Perk> cryomancerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(72, 1, 0, 1, "Fire Aura"));
        perks.add(new PerkBaseStat(73, 1, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkBaseStat(74, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(75, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(76, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(77, 1, 0, 1, "Fire Blast"));
        perks.add(new PerkBaseStat(78, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(79, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(80, 1, 0, 1, "Meteor Shower"));
        perks.add(new PerkBaseStat(81, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(82, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(83, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        return perks;
    }

    public static List<Perk> pyromancerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(84, 1, 0, 1, "Fire Aura"));
        perks.add(new PerkBaseStat(85, 1, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkBaseStat(86, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(87, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(88, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(89, 1, 0, 1, "Fire Blast"));
        perks.add(new PerkBaseStat(90, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(91, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(92, 1, 0, 1, "Meteor Shower"));
        perks.add(new PerkBaseStat(93, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(94, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(95, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        return perks;
    }

    public static List<Perk> warlockPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(96, 1, 0, 1, "Fire Aura"));
        perks.add(new PerkBaseStat(97, 1, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkBaseStat(98, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(99, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(100, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkSpell(101, 1, 0, 1, "Fire Blast"));
        perks.add(new PerkBaseStat(102, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(103, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 5));
        perks.add(new PerkSpell(104, 1, 0, 1, "Meteor Shower"));
        perks.add(new PerkBaseStat(105, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(106, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        perks.add(new PerkBaseStat(107, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 3));
        return perks;
    }
}
