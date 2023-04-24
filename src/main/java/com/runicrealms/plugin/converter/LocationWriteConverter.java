package com.runicrealms.plugin.converter;

import org.bson.Document;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class LocationWriteConverter implements Converter<Location, Document> {

    @Override
    public Document convert(@NotNull Location source) {
        Document document = new Document();
        assert source.getWorld() != null;
        document.put("world", source.getWorld().getName());
        document.put("x", source.getX());
        document.put("y", source.getY());
        document.put("z", source.getZ());
        document.put("yaw", source.getYaw());
        document.put("pitch", source.getPitch());
        return document;
    }

}
