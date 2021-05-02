package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.player.stat.BaseStatEnum;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

public class ArcherTreeUtil {

    public static Perk DEFAULT_ARCHER_SPELL_PERK = new PerkSpell(995, 0, 1, 1, "Barrage");

    public static List<Perk> marksmanPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(0, 1, 0, 1, "Power Shot"));
        perks.add(new PerkBaseStat(1, 1, 0, 5, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(2, 1, 0, 1, "Hawkeye"));
        perks.add(new PerkBaseStat(3, 1, 0, 3, BaseStatEnum.INTELLIGENCE));
        perks.add(new PerkBaseStat(4, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(5, 1, 0, 1, "Parry"));
        perks.add(new PerkBaseStat(6, 1, 0, 3, BaseStatEnum.INTELLIGENCE));
        perks.add(new PerkBaseStat(7, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(8, 1, 0, 1, "Kill Shot"));
        perks.add(new PerkBaseStat(9, 1, 0, 3, BaseStatEnum.INTELLIGENCE));
        perks.add(new PerkBaseStat(10, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(11, 1, 0, 1, "Bear Trap"));
        return perks;
    }

    public static List<Perk> scoutPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(12, 1, 0, 1, "Grapple"));
        perks.add(new PerkBaseStat(13, 1, 0, 5, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(14, 1, 0, 1, "Wing Clip"));
        perks.add(new PerkBaseStat(15, 1, 0, 3, BaseStatEnum.INTELLIGENCE));
        perks.add(new PerkBaseStat(16, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(17, 1, 0, 1, "Flare"));
        perks.add(new PerkBaseStat(18, 1, 0, 3, BaseStatEnum.INTELLIGENCE));
        perks.add(new PerkBaseStat(19, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(20, 1, 0, 1, "Summon Sentry"));
        perks.add(new PerkBaseStat(21, 1, 0, 3, BaseStatEnum.INTELLIGENCE));
        perks.add(new PerkBaseStat(22, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(23, 1, 0, 1, "Escape Artist"));
        return perks;
    }

    public static List<Perk> wardenPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(24, 1, 0, 1, "Arcane Shot"));
        perks.add(new PerkBaseStat(25, 1, 0, 5, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(26, 1, 0, 1, "Icebrand"));
        perks.add(new PerkBaseStat(27, 1, 0, 3, BaseStatEnum.VITALITY));
        perks.add(new PerkBaseStat(28, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(29, 1, 0, 1, "Ice Volley"));
        perks.add(new PerkBaseStat(30, 1, 0, 3, BaseStatEnum.VITALITY));
        perks.add(new PerkBaseStat(31, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(32, 1, 0, 1, "Binding Shot"));
        perks.add(new PerkBaseStat(33, 1, 0, 3, BaseStatEnum.VITALITY));
        perks.add(new PerkBaseStat(34, 1, 0, 3, BaseStatEnum.DEXTERITY));
        perks.add(new PerkSpell(35, 1, 0, 1, "Survival Instinct"));
        return perks;
    }
}
