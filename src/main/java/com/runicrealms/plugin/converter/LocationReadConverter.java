package com.runicrealms.plugin.converter;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;


@ReadingConverter
public class LocationReadConverter implements Converter<Document, Location> {

    @Override
    public Location convert(@NotNull Document source) {
        String world = source.getString("world");
        double x = source.getDouble("x");
        double y = source.getDouble("y");
        double z = source.getDouble("z");
        double yaw = source.getDouble("yaw");
        double pitch = source.getDouble("pitch");
        return new Location(Bukkit.getWorld(world), x, y, z, (float) yaw, (float) pitch);
    }

}
