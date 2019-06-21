package com.runicrealms.plugin.spellapi.spellutil;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class KnockbackUtil {

    private static final double POWER = 0.35;

    public static void knockback(Player p, Entity t) {
        Location l = t.getLocation().subtract(p.getLocation());
        double distance = t.getLocation().distance(p.getLocation());
        Vector v = l.toVector().multiply(POWER/distance);
        v.setY(0.3333);
        t.setVelocity(v);
    }

    // todo: fix this
    public static void knockback(Entity damager, Entity t) {
        Location l = t.getLocation().subtract(damager.getLocation());
        double distance = t.getLocation().distance(damager.getLocation());
        Vector v = l.toVector().normalize().multiply(POWER/distance);
//        DecimalFormat decimalFormat = new DecimalFormat("#.####");
//        v.setX(Float.parseFloat(decimalFormat.format(v.getX())));
        v.setY(0.3333);
        t.setVelocity(v);
    }

    public static void knockbackMob(Entity attacker, Entity t) {
        Location l = t.getLocation().subtract(attacker.getLocation());
        double distance = t.getLocation().distance(attacker.getLocation());
        Vector v = l.toVector().multiply(POWER/(distance * 2.5));
        v.setY(0.25);
        t.setVelocity(v);
    }

    public static void knockback(Player p, Entity t, double mult) {
        Location l = t.getLocation().subtract(p.getLocation());
        double distance = t.getLocation().distance(p.getLocation());
        Vector v = l.toVector().multiply(mult/distance);
        v.setY(0.3333);
        t.setVelocity(v);
    }

//    public static void arrowKnockback(Player p, Entity t)
//    {
//        Location l = t.getLocation().subtract(p.getLocation());
//        double distance = t.getLocation().distance(p.getLocation());
//        Vector v = l.toVector().multiply(1/(2*distance));
//        v.setY(0.3333/2);
//        t.setVelocity(v);
//    }
}

