package com.runicrealms.plugin.database;

import org.bson.Document;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

public interface ICharacterAPI {

    Object getCharacter(Document playerFile, int characterSlot); // get player character by slot

    void updateCharacterInv(String uuid, int characterSlot, Inventory inv);

    void updateCharacterLoc(String uuid, int characterSlot, Location loc);

    // TODO: getCharacterQuestProfile
}
