package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.character.CharacterManager;
import org.bukkit.entity.Player;

public class CharacterApi {

    public static Integer getCurrentCharacterSlot(Player player) {
        return CharacterManager.getSelectedCharacters().get(player.getUniqueId());
    }

    public static boolean hasSelectedCharacter(Player player) {
        return CharacterManager.getSelectedCharacters().get(player.getUniqueId()) != null;
    }

}
