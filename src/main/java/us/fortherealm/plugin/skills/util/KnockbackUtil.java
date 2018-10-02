package us.fortherealm.plugin.skills.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KnockbackUtil {

    public static void knockback(Player p, Entity t) {
        if(t == null || p == null)
            return;
        Location l = t.getLocation().subtract(p.getLocation());
        double distance = t.getLocation().distance(p.getLocation());
        Vector v = l.toVector().multiply(1/distance);
        v.setY(0.3333);
        t.setVelocity(v);
    }

    public static void arrowKnockback(Player p, Entity t)
    {
        Location l = t.getLocation().subtract(p.getLocation());
        double distance = t.getLocation().distance(p.getLocation());
        Vector v = l.toVector().multiply(1/(2*distance));
        v.setY(0.3333/2);
        t.setVelocity(v);
    }
}
