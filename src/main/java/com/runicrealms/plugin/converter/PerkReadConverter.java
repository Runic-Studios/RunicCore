package com.runicrealms.plugin.converter;

import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.runicitems.Stat;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class PerkReadConverter implements Converter<Document, Perk> {

    @Override
    public Perk convert(@NotNull Document source) {
        if (!source.containsKey("type")) return null;
        if (source.getString("type").equalsIgnoreCase("stat")) {
            return perkBaseStatFromDoc(source);
        } else if (source.getString("type").equalsIgnoreCase("spell")) {
            return perkSpellFromDoc(source);
        }
        return null;
    }

    private PerkBaseStat perkBaseStatFromDoc(Document document) {
        int perkId = document.getInteger("perkId");
        int cost = document.getInteger("cost");
        int currentPoints = document.getInteger("currentPoints");
        int maxPoints = document.getInteger("maxPoints");
        Stat stat = Stat.getFromIdentifier(document.getString("stat"));
        int bonus = document.getInteger("bonus");
        return new PerkBaseStat
                (
                        perkId,
                        cost,
                        currentPoints,
                        maxPoints,
                        stat,
                        bonus
                );
    }

    private PerkSpell perkSpellFromDoc(Document document) {
        int perkId = document.getInteger("perkId");
        int cost = document.getInteger("cost");
        int currentPoints = document.getInteger("currentPoints");
        int maxPoints = document.getInteger("maxPoints");
        String spellName = document.getString("spellName");
        return new PerkSpell
                (
                        perkId,
                        cost,
                        currentPoints,
                        maxPoints,
                        spellName
                );
    }

}
