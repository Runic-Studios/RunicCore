package com.runicrealms.plugin.region;

import net.raidstone.wgevents.events.RegionEnteredEvent;
import net.raidstone.wgevents.events.RegionLeftEvent;
import org.bukkit.WeatherType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class RegionEventListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRegionEntered(RegionEnteredEvent event) {
        if (event.getPlayer() == null) return;
        if (event.getRegionName().equalsIgnoreCase("jorundrskeep")
                || event.getRegionName().equalsIgnoreCase("dead_mans_rest")
                || event.getRegionName().equalsIgnoreCase("blackfrost_spire")) {
            event.getPlayer().setPlayerWeather(WeatherType.DOWNFALL);
        }
    }

    @EventHandler
    public void onRegionLeft(RegionLeftEvent event) {
        if (event.getPlayer() == null) return;
        if (event.getRegionName().equalsIgnoreCase("jorundrskeep")
                || event.getRegionName().equalsIgnoreCase("dead_mans_rest")
                || event.getRegionName().equalsIgnoreCase("blackfrost_spire")) {
            event.getPlayer().setPlayerWeather(WeatherType.CLEAR);
        }
    }

}
