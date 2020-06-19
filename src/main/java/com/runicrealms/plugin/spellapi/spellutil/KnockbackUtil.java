package com.runicrealms.plugin.spellapi.spellutil;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.ActiveMob;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class KnockbackUtil {

    private static final double POWER = 0.35;

    // todo: fix this
    // damager can be mob
    public static void knockbackPlayer(Entity damager, Player victim) {
        Location l = victim.getLocation().subtract(damager.getLocation());
        if (victim.getLocation().getWorld() != damager.getLocation().getWorld()) return;
        double distance = victim.getLocation().distance(damager.getLocation());
        if (distance == 0) distance = 1;
        DecimalFormat finite = new DecimalFormat("#.####");
        //double multiplier = Double.parseDouble(finite.format(POWER/distance));
        double multiplier = POWER/distance;
        Vector vec = l.toVector().normalize().multiply(multiplier);
        vec.setX(Double.parseDouble(finite.format(vec.getX())));
        vec.setY(0.3333);
        vec.setZ(Double.parseDouble(finite.format(vec.getZ())));
        victim.setVelocity(vec);
    }

    public static void knockbackMob(Entity attacker, Entity t) {
        Location l = t.getLocation().subtract(attacker.getLocation());
        //double distance = t.getLocation().distance(attacker.getLocation());
        Vector v = l.toVector().multiply(.05);
        v.setY(0.225);
        // no boss knockback
        if (MythicMobs.inst().getMobManager().getActiveMob(t.getUniqueId()).isPresent()) {
            ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(t.getUniqueId()).get();
            if (am.hasFaction() && am.getFaction().equalsIgnoreCase("boss")) {
                return;
            }
        }
        t.setVelocity(v);
    }

    public static void knockbackRanged(Player p, Entity t) {
//        Location l = t.getLocation().subtract(p.getLocation());
//        double distance = t.getLocation().distance(p.getLocation());
//        Vector v = l.toVector().multiply(1/(2.5*distance));
//        v.setY(0.3333/2.25);
//        t.setVelocity(v);
        Location l = t.getLocation().subtract(p.getLocation());
        //double distance = t.getLocation().distance(attacker.getLocation());
        Vector v = l.toVector().multiply(.025);
        v.setY(0.2);
        if (MythicMobs.inst().getMobManager().getActiveMob(t.getUniqueId()).isPresent()) {
            ActiveMob am = MythicMobs.inst().getMobManager().getActiveMob(t.getUniqueId()).get();
            if (am.hasFaction() && am.getFaction().equalsIgnoreCase("boss")) {
                return;
            }
        }
        t.setVelocity(v);
    }
}

