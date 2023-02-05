package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.model.PlayerSpellData;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.runicitems.Stat;

import java.util.ArrayList;
import java.util.List;

public class ClericTreeUtil {

    public static Perk DEFAULT_CLERIC_SPELL_PERK = new PerkSpell(996, 0, 1, 1, PlayerSpellData.DEFAULT_CLERIC);

    public static List<Perk> bardPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(36, 1, 0, 1, "Warsong"));
        perks.add(new PerkBaseStat(37, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(38, 1, 0, 1, "Encore"));
        perks.add(new PerkBaseStat(39, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(40, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(41, 1, 0, 1, "Accelerando"));
        perks.add(new PerkBaseStat(42, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(43, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(44, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(45, 1, 0, 1, "Discord"));
        perks.add(new PerkBaseStat(46, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(47, 1, 0, 1, "Disband"));
        return perks;
    }

    public static List<Perk> hereticPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(48, 1, 0, 1, "Despair"));
        perks.add(new PerkBaseStat(49, 1, 0, 5, Stat.VITALITY));
        perks.add(new PerkSpell(50, 1, 0, 1, "Touch of Death"));
        perks.add(new PerkBaseStat(51, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(52, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkSpell(53, 1, 0, 1, "Umbral Grasp"));
        perks.add(new PerkBaseStat(54, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(55, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(56, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(57, 1, 0, 1, "Ruination"));
        perks.add(new PerkBaseStat(58, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkSpell(59, 1, 0, 1, "Unholy Water"));
        return perks;
    }

    public static List<Perk> lightbringerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(60, 1, 0, 1, "Sear"));
        perks.add(new PerkBaseStat(61, 1, 0, 5, Stat.WISDOM));
        perks.add(new PerkSpell(62, 1, 0, 1, "Lightwell"));
        perks.add(new PerkBaseStat(63, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(64, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkSpell(65, 1, 0, 1, "Holy Nova"));
        perks.add(new PerkBaseStat(66, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(67, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkBaseStat(68, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(69, 1, 0, 1, "Ray of Light"));
        perks.add(new PerkBaseStat(70, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkSpell(71, 1, 0, 1, "Holy Fire"));
        return perks;
    }
}
