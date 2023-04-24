package com.runicrealms.plugin.api;

import com.runicrealms.plugin.api.event.RegionEnteredEvent;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.session.MoveType;
import com.sk89q.worldguard.session.Session;
import com.sk89q.worldguard.session.handler.Handler;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.util.Set;


/**
 * @author Weby &amp; Anrza (info@raidstone.net)
 * @version 1.0.0
 * @since 3/3/19
 */
public class Entry extends Handler implements Listener {
    public static final Factory factory = new Factory();
    public final PluginManager pm = Bukkit.getPluginManager();

    public Entry(Session session) {
        super(session);
    }

    @Override
    public boolean onCrossBoundary(LocalPlayer player, Location from, Location to,
                                   ApplicableRegionSet toSet, Set<ProtectedRegion> entered,
                                   Set<ProtectedRegion> left, MoveType moveType) {
        for (ProtectedRegion r : entered) {
            RegionEnteredEvent regionEnteredEvent = new RegionEnteredEvent(player.getUniqueId(), r);
            pm.callEvent(regionEnteredEvent);
            if (regionEnteredEvent.isCancelled()) return false;
        }
        return true;
    }

    public static class Factory extends Handler.Factory<Entry> {
        @Override
        public Entry create(Session session) {
            return new Entry(session);
        }
    }


}