package us.fortherealm.plugin.player.utilities;

import de.tr7zw.itemnbtapi.NBTEntity;
import de.tr7zw.itemnbtapi.NBTList;
import de.tr7zw.itemnbtapi.NBTListCompound;
import de.tr7zw.itemnbtapi.NBTType;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;

/**
 * This class controls the changing of the player's health,
 * as well as the way their hearts are displayed.
 * (ex: 50/12.5 = 4 hearts displayed)
 * @author Skyfallin_
 */
public class HealthUtils {

    private static final int baseHealth = 50;
    private static final double divisor = 12.5;

    public static void setBaseHealth(Player pl) {
        setHealth(pl, baseHealth);
    }

    public static void setPlayerHealth(Player pl, double amt) {
        setHealth(pl, amt);
    }

    private static void setHealth(Player pl, double amt) {
        NBTEntity nbtPlayer = new NBTEntity(pl);
        NBTList list = nbtPlayer.getList("Attributes", NBTType.NBTTagCompound);
        for (int i = 0; i < list.size(); i++) {
            NBTListCompound lc = list.getCompound(i);
            if (lc.getString("Name").equals("generic.maxHealth")) {
                lc.setDouble("Base", amt);
            }
        }
    }

    public static void setHeartDisplay(Player pl) {
        // to prevent awkward half-heart displays, it rounds down to the nearest full heart.
        int scale = (int) (pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / divisor);
        if (scale % 2 != 0) {
            scale = scale-1;
        }
        // insurance to prevent "greater than 0" errors on first join
        if (scale <= 0) {
            scale=4;
        }
        pl.setHealthScale(scale);
    }
}
