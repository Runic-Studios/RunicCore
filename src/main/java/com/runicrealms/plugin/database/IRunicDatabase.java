package com.runicrealms.plugin.database;

import com.runicrealms.plugin.player.cache.PlayerCache;
import org.bson.Document;
import org.bukkit.inventory.Inventory;

public interface IRunicDatabase {

    Document getPlayerFile(String uuid); // lookup a player's json data file

    Object getCharacter(Document playerFile, int characterSlot); // get player character by slot

    PlayerCache getPlayerCache(Document playerFile); // returns the in-memory object of the player

    void updateCharacterInv(String uuid, int characterSlot, Inventory inv);

    void updateDocumentField(String uuid, String identifier, Object value);
}
