package com.runicrealms.plugin.codec;

import com.runicrealms.plugin.spellapi.skilltrees.Perk;
import com.runicrealms.plugin.spellapi.skilltrees.PerkBaseStat;
import com.runicrealms.plugin.spellapi.skilltrees.PerkSpell;
import com.runicrealms.runicitems.Stat;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Allows Mongo Spring to store and retrieve the player's character classes
 */
public class PerkCodec implements Codec<Perk> {

    @Override
    public Perk decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        int perkId = reader.readInt32("perkId");
        int cost = reader.readInt32("cost");
        int currentPoints = reader.readInt32("currentPoints");
        int maxPoints = reader.readInt32("maxPoints");
        String type = reader.readString("type");
        if (type.equalsIgnoreCase("spell")) {
            String spellName = reader.readString("spellName");
            reader.readEndDocument();
            return new PerkSpell(perkId, cost, currentPoints, maxPoints, spellName);
        } else if (type.equalsIgnoreCase("stat")) {
            int bonus = reader.readInt32("bonusAmount");
            Stat stat = Stat.getFromIdentifier(reader.readString("stat"));
            reader.readEndDocument();
            return new PerkBaseStat(perkId, cost, currentPoints, maxPoints, stat, bonus);
        }
        return null;
    }

    @Override
    public void encode(BsonWriter writer, Perk perk, EncoderContext encoderContext) {
        if (perk.getCurrentlyAllocatedPoints() > 0) {
            writer.writeStartDocument();
            writer.writeInt32("id", perk.getPerkID());
            writer.writeInt32("currentPoints", perk.getCurrentlyAllocatedPoints());
            writer.writeEndDocument();
        }
    }

    @Override
    public Class<Perk> getEncoderClass() {
        return Perk.class;
    }
}
