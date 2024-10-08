package com.runicrealms.plugin.spellapi.skilltrees.util;

import com.runicrealms.plugin.model.SpellData;
import com.runicrealms.plugin.runicitems.Stat;
import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;

import java.util.ArrayList;
import java.util.List;

public class RogueTreeUtil {

    public static final Perk DEFAULT_ROGUE_SPELL_PERK = new PerkSpell
            (
                    998,
                    0,
                    1,
                    1,
                    SpellData.DEFAULT_ROGUE
            );

    public static List<Perk> corsairPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(132, 1, 0, 1, "Cannonfire"));
        perks.add(new PerkBaseStat(133, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(134, 1, 0, 1, "Scurvy"));
        perks.add(new PerkBaseStat(135, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(136, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(137, 1, 0, 1, "Whirlpool"));
        perks.add(new PerkBaseStat(138, 1, 0, 3, Stat.VITALITY));
        perks.add(new PerkBaseStat(139, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(141, 1, 0, 5, Stat.VITALITY));
        perks.add(new PerkSpell(140, 1, 0, 1, "Harpoon"));
        perks.add(new PerkBaseStat(142, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(143, 1, 0, 1, "Call Of The Deep"));
        return perks;
    }

    public static List<Perk> nightcrawlerPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(108, 1, 0, 1, "Twin Fangs"));
        perks.add(new PerkBaseStat(109, 1, 0, 5, Stat.STRENGTH));
        perks.add(new PerkSpell(110, 1, 0, 1, "Backstab"));
        perks.add(new PerkBaseStat(111, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(112, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(113, 1, 0, 1, "Cocoon"));
        perks.add(new PerkBaseStat(114, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(115, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkBaseStat(117, 1, 0, 5, Stat.DEXTERITY));
        perks.add(new PerkSpell(116, 1, 0, 1, "Unseen"));
        perks.add(new PerkBaseStat(118, 1, 0, 3, Stat.STRENGTH));
        perks.add(new PerkSpell(119, 1, 0, 1, "From The Shadows"));
        return perks;
    }

    public static List<Perk> witchHunterPerkList() {
        List<Perk> perks = new ArrayList<>();
        perks.add(new PerkSpell(120, 1, 0, 1, "Silver Bolt"));
        perks.add(new PerkBaseStat(121, 1, 0, 5, Stat.DEXTERITY));
        perks.add(new PerkSpell(122, 1, 0, 1, "Castigate"));
        perks.add(new PerkBaseStat(123, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(124, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkSpell(125, 1, 0, 1, "Flay"));
        perks.add(new PerkBaseStat(126, 1, 0, 3, Stat.INTELLIGENCE));
        perks.add(new PerkBaseStat(127, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkBaseStat(129, 1, 0, 5, Stat.INTELLIGENCE));
        perks.add(new PerkSpell(128, 1, 0, 1, "Warding Glyph"));
        perks.add(new PerkBaseStat(130, 1, 0, 3, Stat.DEXTERITY));
        perks.add(new PerkSpell(131, 1, 0, 1, "Hereticize"));
        return perks;
    }

}
