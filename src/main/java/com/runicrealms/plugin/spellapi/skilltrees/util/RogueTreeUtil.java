package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

public class RogueTreeUtil {

    public static Perk DEFAULT_ROGUE_SPELL_PERK = new PerkSpell(998, 0, 1, 1, "Sprint");

    public static List<Perk> assassinPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(108, 1, 0, 1, "Smoke Bomb"));
        perks.add(new PerkBaseStat(109, 1, 0, 5, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(110, 1, 0, 1, "Backstab"));
        perks.add(new PerkBaseStat(111, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkBaseStat(112, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(113, 1, 0, 1, "Cloak"));
        perks.add(new PerkBaseStat(114, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkBaseStat(115, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkBaseStat(117, 1, 0, 5, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(116, 1, 0, 1, "Slice and Dice"));
        perks.add(new PerkBaseStat(118, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(119, 1, 0, 1, "Predator"));
        return perks;
    }

    public static List<Perk> duelistPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(120, 1, 0, 1, "Lunge"));
        perks.add(new PerkBaseStat(121, 1, 0, 5, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(122, 1, 0, 1, "Kneebreak"));
        perks.add(new PerkBaseStat(123, 1, 0, 3, BaseStatEnum.VITALITY));
        perks.add(new PerkBaseStat(124, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(125, 1, 0, 1, "Cripple"));
        perks.add(new PerkBaseStat(126, 1, 0, 3, BaseStatEnum.VITALITY));
        perks.add(new PerkBaseStat(127, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkBaseStat(129, 1, 0, 5, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(128, 1, 0, 1, "Riposte"));
        perks.add(new PerkBaseStat(130, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(131, 1, 0, 1, "Challenger"));
        return perks;
    }

    public static List<Perk> swindlerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(132, 1, 0, 1, "Cleanse"));
        perks.add(new PerkBaseStat(133, 1, 0, 5, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(134, 1, 0, 1, "Agility"));
        perks.add(new PerkBaseStat(135, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkBaseStat(136, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(137, 1, 0, 1, "Harpoon"));
        perks.add(new PerkBaseStat(138, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkBaseStat(139, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkBaseStat(141, 1, 0, 5, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(140, 1, 0, 1, "Twist of Fate"));
        perks.add(new PerkBaseStat(142, 1, 0, 3, BaseStatEnum.STRENGTH));
        perks.add(new PerkSpell(143, 1, 0, 1, "Insanity"));
        return perks;
    }
}
