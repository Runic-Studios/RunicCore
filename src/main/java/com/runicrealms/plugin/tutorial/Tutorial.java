package com.runicrealms.plugin.tutorial;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Tutorial {

    /**
     * Call this method when a player enters the main server for the first time.
     */
    public void startTutorial(Player pl) {

        // teleport player to spawn
        pl.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 2));
        Location spawn = new Location(Bukkit.getWorld("Alterra"), -2205, 29, 1902, -35, 0);
        pl.teleport(spawn);

        // set relative time, weather
        pl.setPlayerTime(2100, false);
        pl.setPlayerWeather(WeatherType.CLEAR);
    }

    // todo: reset player time/weather, enable global chat at end of tutorial
}
