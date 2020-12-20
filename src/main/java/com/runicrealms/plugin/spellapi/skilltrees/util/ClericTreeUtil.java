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
        perks.add(new PerkSpell(36, 1, 0, 1, "Warsong"));
        perks.add(new PerkBaseStat(37, 1, 0, 5, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(38, 1, 0, 1, "Improvisation")); // melee attacks X% chance to heal nearby allies or damage nearby enemies
        perks.add(new PerkBaseStat(39, 1, 0, 3, BaseStatEnum.DEXTERITY, 1));
        perks.add(new PerkBaseStat(40, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(41, 1, 0, 1, "Windstride"));
        perks.add(new PerkBaseStat(42, 1, 0, 3, BaseStatEnum.DEXTERITY, 1));
        perks.add(new PerkBaseStat(43, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkBaseStat(44, 1, 0, 5, BaseStatEnum.DEXTERITY, 1));
        perks.add(new PerkSpell(45, 1, 0, 1, "Discord"));
        perks.add(new PerkBaseStat(46, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(47, 1, 0, 1, "Dissonance")); // melee attacks against players have X% chance to swap them to random hotbar slot!
        // against monsters, chance to silence them.
        return perks;
    }

    public static List<Perk> exemplarList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(48, 1, 0, 1, "Smite")); // AoE, high damage, knockback
        perks.add(new PerkBaseStat(49, 1, 0, 5, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(50, 1, 0, 1, "Penance")); // rejuvenate is now penance (white particles), damages monsters if it goes through em
        perks.add(new PerkBaseStat(51, 1, 0, 3, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkBaseStat(52, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(53, 1, 0, 1, "Repent")); // stun like pyro fire blast
        perks.add(new PerkBaseStat(54, 1, 0, 3, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkBaseStat(55, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkBaseStat(56, 1, 0, 5, BaseStatEnum.VITALITY, 1));
        perks.add(new PerkSpell(57, 1, 0, 1, "Consecration")); // AoE, high dmg, slow
        perks.add(new PerkBaseStat(58, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(59, 1, 0, 1, "Righteous Blade")); // your melee attacks have X% chance to heal nearby allies
        return perks;
    }

    public static List<Perk> priestList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(60, 1, 0, 1, "Holy Water")); // should also damage mobs
        perks.add(new PerkBaseStat(61, 1, 0, 5, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(62, 1, 0, 1, "Healing Stream")); // allies hit by holy water receive X% addtional healing for Ys
        perks.add(new PerkBaseStat(63, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkBaseStat(64, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(65, 1, 0, 1, "Holy Aura"));
        perks.add(new PerkBaseStat(66, 1, 0, 3, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkBaseStat(67, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkBaseStat(68, 1, 0, 5, BaseStatEnum.INTELLIGENCE, 1));
        perks.add(new PerkSpell(69, 1, 0, 1, "Soul Link"));
        perks.add(new PerkBaseStat(70, 1, 0, 3, BaseStatEnum.WISDOM, 1));
        perks.add(new PerkSpell(71, 1, 0, 1, "Manawell"));
        return perks;
    }
}
