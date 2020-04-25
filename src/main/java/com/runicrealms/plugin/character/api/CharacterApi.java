package com.runicrealms.plugin.character.api;

import com.runicrealms.plugin.character.CharacterManager;
import com.runicrealms.plugin.character.gui.CharacterGuiManager;
import org.bukkit.entity.Player;

import java.util.Set;

public class CharacterApi {

    public static Integer getCurrentCharacterSlot(Player player) {
        return CharacterManager.getSelectedCharacters().get(player.getUniqueId());
    }

    public static boolean hasSelectedCharacter(Player player) {
        return CharacterManager.getSelectedCharacters().get(player.getUniqueId()) != null;
    }

    public static Set<Integer> getAllCharacters(Player player) {
        if (CharacterGuiManager.getCharacterCache().containsKey(player.getUniqueId())) {
            return CharacterGuiManager.getCharacterCache().get(player.getUniqueId()).getCharacterInfo().keySet();
        }
        return null;
    }

}
