package com.runicrealms.plugin.database;

import java.util.Set;

/**
 * On shutdown, the server saves all characters that a player has used during the session, even if the player is logged out
 * This wrapper stores a single PlayerMongoData object and a set of ints representing the characters to be saved
 *
 * @author Skyfallin
 */
public class ShutdownSaveWrapper {

    private final PlayerMongoData playerMongoData;
    private final Set<Integer> charactersToSave;

    public ShutdownSaveWrapper(PlayerMongoData playerMongoData, Set<Integer> charactersToSave) {
        this.playerMongoData = playerMongoData;
        this.charactersToSave = charactersToSave;
    }

    public PlayerMongoData getPlayerMongoData() {
        return playerMongoData;
    }

    public Set<Integer> getCharactersToSave() {
        return charactersToSave;
    }
}
