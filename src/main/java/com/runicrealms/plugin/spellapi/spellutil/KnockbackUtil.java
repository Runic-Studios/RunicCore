package com.runicrealms.plugin.spellapi.spellutil;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KnockbackUtil {

    private static final double POWER = 0.35;

//    public static void knockbackPlayer(Player p, Entity t) {
//        Location l = t.getLocation().subtract(p.getLocation());
//        double distance = t.getLocation().distance(p.getLocation());
//        Vector v = l.toVector().multiply(POWER/distance);
//        v.setY(0.3333);
//        t.setVelocity(v);
//    }

    // todo: fix this
    // damager can be mob
    public static void knockbackPlayer(Entity damager, Player victim) {
        Location l = victim.getLocation().subtract(damager.getLocation());
        double distance = victim.getLocation().distance(damager.getLocation());
        Vector vec = l.toVector().normalize().multiply(POWER/distance);
        vec.setY(0.3333);
        victim.setVelocity(vec);
    }

    public static void knockbackMob(Entity attacker, Entity t) {
        Location l = t.getLocation().subtract(attacker.getLocation());
        //double distance = t.getLocation().distance(attacker.getLocation());
        Vector v = l.toVector().multiply(.1);
        v.setY(0.25);
        t.setVelocity(v);
    }

    public static void knockbackPlayer(Player p, Entity t, double mult) {
        Location l = t.getLocation().subtract(p.getLocation());
        double distance = t.getLocation().distance(p.getLocation());
        Vector v = l.toVector().multiply(mult/distance);
        v.setY(0.3333);
        t.setVelocity(v);
    }

    public static void knockbackRanged(Player p, Entity t) {
        Location l = t.getLocation().subtract(p.getLocation());
        double distance = t.getLocation().distance(p.getLocation());
        Vector v = l.toVector().multiply(1/(2*distance));
        v.setY(0.3333/2);
        t.setVelocity(v);
    }
}

