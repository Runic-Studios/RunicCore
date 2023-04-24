package com.runicrealms.plugin.converter;

import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class PerkWriteConverter implements Converter<Perk, Document> {

    @Override
    public Document convert(@NotNull Perk source) {
        Document document = new Document();
        if (source instanceof PerkBaseStat) {
            return source.writeToDocument(source, document);
        } else if (source instanceof PerkSpell) {
            return source.writeToDocument(source, document);
        }
        return document;
    }

}
