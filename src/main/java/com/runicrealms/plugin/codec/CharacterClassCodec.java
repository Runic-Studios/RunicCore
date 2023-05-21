package com.runicrealms.plugin.codec;

import com.runicrealms.plugin.common.CharacterClass;
import com.runicrealms.plugin.rdb.model.CharacterField;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Allows Mongo Spring to store and retrieve the player's character classes
 */
public class CharacterClassCodec implements Codec<CharacterClass> {

    @Override
    public CharacterClass decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        String name = reader.readString(CharacterField.CLASS_TYPE.getField());
        reader.readEndDocument();
        return CharacterClass.getFromName(name);
    }

    @Override
    public void encode(BsonWriter writer, CharacterClass characterClass, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeString(CharacterField.CLASS_LEVEL.getField(), characterClass.getName());
        writer.writeEndDocument();
    }

    @Override
    public Class<CharacterClass> getEncoderClass() {
        return CharacterClass.class;
    }
}
