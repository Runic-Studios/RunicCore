package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores all default perk lists for the Mage class
 *
 * @author Skyfallin
 */
public class MageTreeUtil {

    public static final Perk DEFAULT_MAGE_SPELL_PERK = new PerkSpell(997, 0, 1, 1, SpellData.DEFAULT_MAGE);

    public static List<Perk> cryomancerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(72, 1, 0, 1, "Frostbite"));
        perks.add(new PerkBaseStat(73, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(74, 1, 0, 1, "Shatter"));
        perks.add(new PerkBaseStat(75, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(76, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(77, 1, 0, 1, "Snap Freeze"));
        perks.add(new PerkBaseStat(78, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(79, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(80, 1, 0, 5, Stat.VITALITY));
        perks.add(new PerkSpell(81, 1, 0, 1, "Blizzard"));
        perks.add(new PerkBaseStat(82, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(83, 1, 0, 1, "Glacier"));
        return perks;
    }

    public static List<Perk> pyromancerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(84, 1, 0, 1, "Dragon's Breath"));
        perks.add(new PerkBaseStat(85, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(86, 1, 0, 1, "Incendiary"));
        perks.add(new PerkBaseStat(87, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(88, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(89, 1, 0, 1, "Erupt"));
        perks.add(new PerkBaseStat(90, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(91, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(92, 1, 0, 5, Stat.DEXTERITY));
        perks.add(new PerkSpell(93, 1, 0, 1, "Meteor"));
        perks.add(new PerkBaseStat(94, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(95, 1, 0, 1, "Wildfire"));
        return perks;
    }

    public static List<Perk> spellswordPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(96, 1, 0, 1, "Arcane Slash"));
        perks.add(new PerkBaseStat(97, 1, 0, 5, Stat.WISDOM));
        perks.add(new PerkSpell(98, 1, 0, 1, "Spectral Blade"));
        perks.add(new PerkBaseStat(99, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(100, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkSpell(101, 1, 0, 1, "Blink"));
        perks.add(new PerkBaseStat(102, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(103, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkBaseStat(104, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(105, 1, 0, 1, "Manashield"));
        perks.add(new PerkBaseStat(106, 1, 0, 3, Stat.WISDOM));
        perks.add(new PerkSpell(107, 1, 0, 1, "Riftwalk"));
        return perks;
    }

}
