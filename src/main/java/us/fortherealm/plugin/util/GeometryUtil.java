package us.fortherealm.plugin.util;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class GeometryUtil {

    // *** Default list stops projectile from passing solid terrain ***
    public static final List<Material> impassable = new ArrayList<>();
    static {
        Material[] mats = Material.values();
        for (int i = 0; i < mats.length; i++) {
            Material m = mats[i];
            if (m.isSolid()) impassable.add(m);
        }
    }
}
