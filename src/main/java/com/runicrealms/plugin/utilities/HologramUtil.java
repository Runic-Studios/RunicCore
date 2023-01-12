package com.runicrealms.plugin.utilities;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.runicrealms.plugin.RunicCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

import java.util.Collections;
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
        double yDif = (randInt() - 10) / 10D;
        double zDif = -0.5 + (int) (Math.random() * ((0.5 - (-0.5)) + 1));
        if (zDif == 0) {
            zDif = 0.5;
        }
        Hologram hologram = HologramsAPI.createHologram(RunicCore.getInstance(), location.add(xDif, yDif, zDif).subtract(0, 1, 0));
        hologram.appendTextLine(lineToDisplay);
        if (players == null) {
            hologram.getVisibilityManager().setVisibleByDefault(true);
        } else {
            hologram.getVisibilityManager().setVisibleByDefault(false);
            for (Player player : players) {
                hologram.getVisibilityManager().showTo(player);
            }
        }
        Bukkit.getScheduler().runTaskLater(RunicCore.getInstance(), hologram::delete, 30L); // 1.5s
    }

    /**
     * Used in place of damage holograms for when players are fighting each other
     *
     * @param caster       the hologram is client-sided, only displays for this player
     * @param victim       the player who took damage
     * @param createAround the location to spawn around (location is slightly random)
     */
    public static void createHealthBarHologram(Player caster, Player victim, Location createAround) {
        createAround.add(0, 1, 0);
        int healthToDisplay = (int) (victim.getHealth());
        int maxHealth = (int) victim.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double healthPercent = (double) healthToDisplay / maxHealth;
        ChatColor chatColor;
        if (healthPercent >= .75) {
            chatColor = ChatColor.GREEN;
        } else if (healthPercent >= .5) {
            chatColor = ChatColor.YELLOW;
        } else if (healthPercent >= .25) {
            chatColor = ChatColor.RED;
        } else {
            chatColor = ChatColor.DARK_RED;
        }
        createCombatHologram
                (
                        Collections.singletonList(caster),
                        createAround,
                        chatColor + "" + healthToDisplay + " HP"

                );
    }

}
