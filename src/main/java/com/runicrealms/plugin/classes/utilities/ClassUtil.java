package com.runicrealms.plugin.classes.utilities;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

public class ClassUtil {

    /**
     * Launches a firework of the specified color.
     */
    public static void launchFirework(Player p, Color color) {
        Firework firework = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).build());
        firework.setFireworkMeta(meta);
    }

    /**
     * Overloaded method which takes in a className, automatically chooses the color
     */
    public static void launchFirework(Player p, String className) {
        Firework firework = p.getWorld().spawn(p.getEyeLocation(), Firework.class);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.setPower(0);
        Color color = Color.WHITE;
        switch (className) {
            case "Archer":
                color = Color.LIME;
                break;
            case "Cleric":
                color = Color.AQUA;
                break;
            case "Mage":
                color = Color.FUCHSIA;
                break;
            case "Rogue":
                color = Color.YELLOW;
                break;
            case "Warrior":
                color = Color.RED;
                break;
        }
        meta.addEffect(FireworkEffect.builder().with(FireworkEffect.Type.BALL_LARGE).withColor(color).build());
        firework.setFireworkMeta(meta);
    }
}
