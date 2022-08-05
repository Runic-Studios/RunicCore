package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.api.RunicCoreAPI;
import org.bukkit.entity.Player;

import java.util.Set;

public class CharacterApi {

    public static Integer getCurrentCharacterSlot(Player player) {
        return RunicCoreAPI.getCharacterSlot(player.getUniqueId());
    }

    public static boolean hasSelectedCharacter(Player player) {
        return RunicCoreAPI.getLoadedCharacters().contains(player.getUniqueId());
    }

    public static Set<Integer> getAllCharacters(Player player) {
        if (RunicCoreAPI.getPlayerData(player.getUniqueId()) != null) {
            return RunicCoreAPI.getPlayerData(player.getUniqueId()).getPlayerCharacters().keySet();
        }
        return null;
    }

}
