package com.runicrealms.plugin.music;

import com.runicrealms.plugin.RunicCore;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

/**
 * Plays different music tracks from the resource pack when certain areas are entered
 */
public class MusicListener implements Listener {
    private static final HashMap<String, String> MUSIC_REGION_MAP = new HashMap<>() {{
        put("tutorial", "verdant_realm");
        put("azana", "verdant_realm");
        put("jorundrskeep", "jorundrs_keep");
    }};

    @EventHandler
    public void onRegionEntered(RegionEnteredEvent event) {
        if (MUSIC_REGION_MAP.isEmpty()) return;
        String regionName = event.getRegionName().toLowerCase();
        // Ensure there is music for this region
        if (!MUSIC_REGION_MAP.containsKey(regionName)) return;
        Player player = event.getPlayer();
        if (player == null) return;
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), () -> {
            // Play music
            player.playSound
                    (
                            player.getLocation(),
                            "music." + MUSIC_REGION_MAP.get(regionName),
                            SoundCategory.MUSIC,
                            0.5f,
                            1.0f
                    );
        }, 20L);
    }

    @EventHandler
    public void onRegionLeft(RegionLeftEvent event) {
        if (MUSIC_REGION_MAP.isEmpty()) return;
        String regionName = event.getRegionName().toLowerCase();
        // Ensure there is music for this region
        if (!MUSIC_REGION_MAP.containsKey(regionName)) return;
        Player player = event.getPlayer();
        if (player == null) return;
        MUSIC_REGION_MAP.forEach((region, song) -> player.stopSound("music." + song, SoundCategory.MUSIC));
    }

}
