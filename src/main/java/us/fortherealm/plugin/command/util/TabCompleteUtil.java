package us.fortherealm.plugin.command.util;

import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
//import java.utilities.Collections;
import java.util.List;

public class TabCompleteUtil {

    public static List<String> getPlayers(CommandSender commandSender, String[] strings, Plugin plugin) {
        if (strings.length == 0) {
            return ImmutableList.of();
        }

        String lastWord = strings[strings.length - 1];

        Player senderPlayer = commandSender instanceof Player ? (Player) commandSender : null;

        ArrayList<String> matchedPlayers = new ArrayList<String>();
        for (Player player : commandSender.getServer().getOnlinePlayers()) {
            String name = plugin.getConfig().get(player.getUniqueId() + ".info.name").toString();
            if ((senderPlayer == null || senderPlayer.canSee(player)) && StringUtil.startsWithIgnoreCase(name, lastWord)) {
                matchedPlayers.add(name);
            }
        }

        //Collections.sort(matchedPlayers, String.CASE_INSENSITIVE_ORDER);
        matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
        return matchedPlayers;
    }
}
