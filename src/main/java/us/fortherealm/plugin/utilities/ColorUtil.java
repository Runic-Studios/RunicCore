package us.fortherealm.plugin.utilities;

import org.bukkit.ChatColor;

public class ColorUtil {

    public static String format(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
