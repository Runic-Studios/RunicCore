package com.runicrealms.plugin.codec;

import com.runicrealms.plugin.model.SkillTreePosition;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

/**
 * Allows Mongo Spring to store and retrieve the player's character classes
 */
public class SkillTreePositionCodec implements Codec<SkillTreePosition> {

    @Override
    public SkillTreePosition decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartDocument();
        int value = reader.readInt32("position");
        reader.readEndDocument();
        return SkillTreePosition.getFromValue(value);
    }

    @Override
    public void encode(BsonWriter writer, SkillTreePosition skillTreePosition, EncoderContext encoderContext) {
        writer.writeStartDocument();
        writer.writeInt32("position", skillTreePosition.getValue());
        writer.writeEndDocument();
    }

    @Override
    public Class<SkillTreePosition> getEncoderClass() {
        return SkillTreePosition.class;
    }
}
