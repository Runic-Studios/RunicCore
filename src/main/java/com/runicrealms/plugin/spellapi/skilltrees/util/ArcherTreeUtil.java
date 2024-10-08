package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

public class ArcherTreeUtil {

    public static final Perk DEFAULT_ARCHER_SPELL_PERK = new PerkSpell(995, 0, 1, 1, SpellData.DEFAULT_ARCHER);

    public static List<Perk> marksmanPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(0, 1, 0, 1, "Piercing Arrow"));
        perks.add(new PerkBaseStat(1, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(2, 1, 0, 1, "Ambush"));
        perks.add(new PerkBaseStat(3, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(4, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(5, 1, 0, 1, "Leaping Shot"));
        perks.add(new PerkBaseStat(6, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(7, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(9, 1, 0, 5, Stat.DEXTERITY));
        perks.add(new PerkSpell(8, 1, 0, 1, "Rain of Arrows"));
        perks.add(new PerkBaseStat(10, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(11, 1, 0, 1, "Steady Aim"));
        return perks;
    }

    public static List<Perk> stormshotPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(24, 1, 0, 1, "Thunder Arrow"));
        perks.add(new PerkBaseStat(25, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(26, 1, 0, 1, "Stormborn"));
        perks.add(new PerkBaseStat(27, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(28, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(29, 1, 0, 1, "Surge"));
        perks.add(new PerkBaseStat(30, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(31, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(33, 1, 0, 5, Stat.DEXTERITY));
        perks.add(new PerkSpell(32, 1, 0, 1, "Jolt"));
        perks.add(new PerkBaseStat(34, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(35, 1, 0, 1, "Overcharge"));
        return perks;
    }

    public static List<Perk> wardenPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(12, 1, 0, 1, "Snare Trap"));
        perks.add(new PerkBaseStat(13, 1, 0, 5, Stat.WISDOM));
        perks.add(new PerkSpell(14, 1, 0, 1, "Refreshing Volley"));
        perks.add(new PerkBaseStat(15, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(16, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkSpell(17, 1, 0, 1, "Remedy"));
        perks.add(new PerkBaseStat(18, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(19, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkBaseStat(21, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(20, 1, 0, 1, "Sacred Grove"));
        perks.add(new PerkBaseStat(22, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkSpell(23, 1, 0, 1, "Gifts Of The Grove"));
        return perks;
    }
}
