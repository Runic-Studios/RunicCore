package com.runicrealms.plugin;

import com.runicrealms.plugin.utilities.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public enum TravelLocation {
    AZANA("azana", "Azana", new Location(Bukkit.getWorld("Alterra"), -998.5, 34, 170.5, 270, 0)),
    KOLDORE("koldore", "Koldore", new Location(Bukkit.getWorld("Alterra"), -1539.5, 33, 203.5, 90, 0)),
    WHALETOWN("whaletown", "Whaletown", new Location(Bukkit.getWorld("Alterra"), -1789.5, 35, -698.5, 0, 0)),
    HILSTEAD("hilstead", "Hilstead", new Location(Bukkit.getWorld("Alterra"), -1688.5, 44, -2069.5, 90, 0)),
    WINTERVALE("wintervale", "Wintervale", new Location(Bukkit.getWorld("Alterra"), -1640.5, 33, -2624.5, 90, 0)),
    DEAD_MANS_REST("dead_mans_rest", "Dead Man's Rest", new Location(Bukkit.getWorld("Alterra"), -109.5, 31, -516.5, 270, 0)),
    ISFODAR("isfodar", "Isfodar", new Location(Bukkit.getWorld("Alterra"), 499.5, 60, -86.5, 270, 0)),
    TIRNEAS("tireneas", "Tireneas", new Location(Bukkit.getWorld("Alterra"), 899.5, 36, 581.5, 90, 0)),
    ZENYTH("zenyth", "Zenyth", new Location(Bukkit.getWorld("Alterra"), 1583.5, 34, -196.5, 270, 0)),
    NAHEEN("naheen", "Naheen", new Location(Bukkit.getWorld("Alterra"), 1861.5, 39, 154.5, 315, 0)),
    NAZMORA("nazmora", "Naz'mora", new Location(Bukkit.getWorld("Alterra"), 2608.5, 33, 998.5, 0, 0)),
    ORC_OUTPOST("orc_outpost", "Orc Outpost", new Location(Bukkit.getWorld("Alterra"), 2409.5, 26, 1720.5, 0, 0)),
    FROSTS_END("frosts_end", "Frost's End", new Location(Bukkit.getWorld("Alterra"), 1116.5, 33, 2576.5, 90, 0)),
    /*
    Conquest Points
     */
    SUNS_REACH_CITADEL("suns_reach_citadel", "Sun's Reach Citadel", new Location(Bukkit.getWorld("Alterra"), 2439.5, 29, -1172.5, 250, 0)),
    BLACKGUARD_STRONGHOLD("blackguard_stronghold", "Blackguard Stronghold", new Location(Bukkit.getWorld("Alterra"), 2136.5, 34, -2170.5, 180, 0)),
    CRIMSON_CHAPEL("crimson_chapel", "Crimson Chapel", new Location(Bukkit.getWorld("Alterra"), 1453.5, 24, -1119.5, 270, 0));

    private final String identifier;
    private final String display;
    private final Location location;

    TravelLocation(String identifier, String display, Location location) {
        this.identifier = identifier;
        this.display = display;
        this.location = location;
    }

    public static TravelLocation getFromIdentifier(String identifier) {
        for (TravelLocation travelLocation : TravelLocation.values()) {
            if (travelLocation.getIdentifier().equals(identifier))
                return travelLocation;
        }
        return AZANA; // default if something went wrong
    }

    public static TravelLocation getFromLocation(Location location) {
        for (TravelLocation travelLocation : TravelLocation.values()) {
            if (travelLocation.getLocation().equals(location))
                return travelLocation;
        }
        return AZANA; // default if something went wrong
    }

    public static void fastTravelTask(Player player, TravelType travelType, TravelLocation travelLocation) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 5 * 20, 2));
        player.teleport(travelLocation.getLocation());

        new BukkitRunnable() {
            int count = 1;

            @Override
            public void run() {

                if (count > 5) {
                    this.cancel();
                    player.sendMessage(ColorUtil.format("&aYou arrive at your destination!"));
                } else {
                    count += 1;
                    player.playSound(player.getLocation(), travelType.getSound(), 0.5f, 1.0f);
                }
            }
        }.runTaskTimerAsynchronously(RunicCore.getInstance(), 0, 20L);
    }

    public String getDisplay() {
        return display;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Location getLocation() {
        return location;
    }
}
