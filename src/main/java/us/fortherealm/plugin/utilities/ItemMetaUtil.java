package us.fortherealm.plugin.utilities;

import org.bukkit.ChatColor;

public class ItemMetaUtil {

    private ItemMetaUtil() {}

    public static String hideLore(String lore) {
        StringBuilder sb = new StringBuilder();
        for(char c : lore.toCharArray()) {
            sb.append(ChatColor.COLOR_CHAR + String.valueOf(c));
        }
        return sb.toString();
    }

    public static String revealHiddenLore(String lore) {
        return lore.replace(String.valueOf(ChatColor.COLOR_CHAR), "");
    }

}
