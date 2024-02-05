package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.DungeonLocation;
import com.runicrealms.plugin.SafeZoneLocation;
import com.runicrealms.plugin.api.RegionAPI;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public final class RegionHelper implements RegionAPI {

    @Override
    public boolean containsRegion(Location location, String regionIdentifier) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return false;
        for (ProtectedRegion region : regions) {
            if (region.getId().contains(regionIdentifier))
                return true;
        }
        return false;
    }

    @Override
    public DungeonLocation getDungeonFromLocation(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return null;
        for (ProtectedRegion region : regions) {
            for (DungeonLocation dungeonLocation : DungeonLocation.values()) {
                if (region.getId().contains(dungeonLocation.getRegionIdentifier()))
                    return dungeonLocation;
            }
        }
        return null;
    }

    @Override
    public List<String> getRegionIds(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet set = query.getApplicableRegions(com.sk89q.worldedit.bukkit.BukkitAdapter.adapt(location));
        Set<ProtectedRegion> regions = set.getRegions();
        if (regions == null) return new ArrayList<>();
        List<String> regionIds = new ArrayList<>();
        for (ProtectedRegion region : regions) {
            regionIds.add(region.getId());
        }
        return regionIds;
    }

    @Override
    public boolean isSafezone(Location location) {
        List<String> regionIds = getRegionIds(location);
        for (String regionId : regionIds) {
            if (Arrays.stream(SafeZoneLocation.values()).anyMatch(cityLocation -> regionId.equals(cityLocation.getIdentifier())))
                return true;
        }
        return false;
    }
}
