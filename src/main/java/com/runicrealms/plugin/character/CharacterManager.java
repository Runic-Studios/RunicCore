package com.runicrealms.plugin.character;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterManager {

    private static Map<UUID, Integer> selectedCharacters = new HashMap<UUID, Integer>();

    public static Map<UUID, Integer> getSelectedCharacters() {
        return selectedCharacters;
    }

}
