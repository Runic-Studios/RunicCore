package com.runicrealms.plugin.utilities;

import com.runicrealms.plugin.RunicCore;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class HologramUtil {

    private static int randInt() {
        int bound = 20 + 1;
        return ThreadLocalRandom.current().nextInt(bound);
    }

    /**
     * Creates a hologram for relevant players during combat
     *
     * @param players       a list of players to display hologram to. set to null to display to all
     * @param location      of the hologram
     * @param lineToDisplay the contents of the hologram
     */
    public static void createCombatHologram(List<Player> players, Location location, String lineToDisplay) {
        double xDif = -0.5 + (int) (Math.random() * ((0.5 - (-0.5)) + 1));
        if (xDif == 0) {
            xDif = 0.5;
        }
        double yDif = (randInt() - 7.5) / 7.5D; // todo: 5
        double zDif = -0.5 + (int) (Math.random() * ((0.5 - (-0.5)) + 1));
        if (zDif == 0) {
            zDif = 0.5;
        }
        Hologram hologram = HolographicDisplaysAPI.get(RunicCore.getInstance()).createHologram(location.add(xDif, yDif, zDif));
        hologram.getLines().appendText(lineToDisplay);
        if (players == null) {
            hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
        } else {
            hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
            for (Player player : players) {
                hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
            }
        }
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), hologram::delete, 30L); // 1.5s
    }

}
