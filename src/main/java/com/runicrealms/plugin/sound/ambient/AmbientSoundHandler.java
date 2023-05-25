package com.runicrealms.plugin.sound.ambient;

import com.runicrealms.plugin.RunicCore;
import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.Bukkit;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;

/**
 * Plays different music tracks from the resource pack when certain areas are entered
 */
public class AmbientSoundHandler implements Listener {
    private static final int PERIOD = 10;
    private static final HashMap<String, List<String>> AMBIENT_SOUND_MAP;
    private static final Set<Player> PLAYERS_IN_REGION = new HashSet<>();

    static {
        AMBIENT_SOUND_MAP = new HashMap<>();
        List<String> forestSounds = new ArrayList<>(Arrays.asList
                (
                        "frog_idle1",
                        "frog_idle2",
                        "bird_fly",
                        "bird_song",
                        "bird_song2",
                        "bird_song3",
                        "bird_song4",
                        "owl_idle",
                        "raccoon_idle"
                ));
        AMBIENT_SOUND_MAP.put("silkwood", forestSounds);
    }

    public AmbientSoundHandler() {
        Bukkit.getPluginManager().registerEvents(this, RunicCore.getInstance());
        Bukkit.getScheduler().runTaskTimerAsynchronously(RunicCore.getInstance(), () -> {
            if (PLAYERS_IN_REGION.isEmpty()) return;
            Bukkit.broadcastMessage("playing music");
            // Play music
            PLAYERS_IN_REGION.forEach(player -> player.playSound
                    (
                            player.getLocation(),
                            "littleroom_wilderness:littleroom.wilderness.bird_song", // todo: random
                            SoundCategory.AMBIENT,
                            0.5f,
                            1.0f
                    ));
        }, 0, PERIOD * 20L);
    }

    @EventHandler
    public void onRegionEntered(RegionEnteredEvent event) {
        if (AMBIENT_SOUND_MAP.isEmpty()) return;
        String regionName = event.getRegionName().toLowerCase();
        // Ensure there is music for this region
        if (!AMBIENT_SOUND_MAP.containsKey(regionName)) return;
        Player player = event.getPlayer();
        if (player == null) return;
        PLAYERS_IN_REGION.add(player);
    }

    @EventHandler
    public void onRegionLeft(RegionLeftEvent event) {
        if (AMBIENT_SOUND_MAP.isEmpty()) return;
        String regionName = event.getRegionName().toLowerCase();
        // Ensure there is music for this region
        if (!AMBIENT_SOUND_MAP.containsKey(regionName)) return;
        Player player = event.getPlayer();
        if (player == null) return;
        PLAYERS_IN_REGION.remove(player);
    }

}
